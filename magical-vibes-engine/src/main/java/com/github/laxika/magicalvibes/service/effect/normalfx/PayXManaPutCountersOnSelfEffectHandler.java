package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaPutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayXManaPutCountersOnSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaPutCountersOnSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PayXManaPutCountersOnSelfEffect) effect;
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;

            if (chosenValue == 0) {
                gameLogService.append(gameData, GameLog.text(playerName + " chooses X=0 for " + cardName + "'s ability."));
                log.info("Game {} - {} chooses X=0 for {}", gameData.id, playerName, cardName);
                return;
            }

            ManaPool pool = gameData.playerManaPools.get(controllerId);
            if (payableFromPool(pool) < chosenValue) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " can't pay {" + chosenValue + "} for " + cardName
                                + " (tap mana sources, then choose X again)."));
                log.info("Game {} - {} cannot yet pay X={} for {} — re-prompting",
                        gameData.id, playerName, chosenValue, cardName);
                beginXPrompt(gameData, controllerId, cardName, e.useTriggeringPermanent());
                return;
            }

            new ManaCost("{X}").payResolutionCost(pool, chosenValue);
            gameLogService.append(gameData, GameLog.text(playerName + " pays {" + chosenValue + "} for " + cardName + "."));
            log.info("Game {} - {} pays {} mana for {}", gameData.id, playerName, chosenValue, cardName);

            UUID selfId = e.useTriggeringPermanent() && entry.getTriggeringPermanentId() != null
                    ? entry.getTriggeringPermanentId()
                    : entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
            if (e.reflexive()) {
                if (selfId != null) {
                    // "When you do, put X +1/+1 counters on this creature" (or "on that creature", the one that
                    // entered): the ability targets nothing and records what the resolving entry was about, with
                    // the X paid as its X.
                    CardEffect putCounters = e.useTriggeringPermanent() && entry.getTriggeringPermanentId() != null
                            ? new PutCounterOnReferencedPermanentEffect(
                                    PermanentReference.TRIGGERING, e.counterType(), new XValue())
                            : new PutCountersOnSelfEffect(e.counterType(), new XValue());
                    StackEntry reflexive = ReflexiveAbilitySupport.record(gameData, entry, List.of(putCounters));
                    reflexive.setXValue(chosenValue);
                    triggeredAbilityStackService.putMade(gameData, reflexive);
                }
                return;
            }
            Permanent self = gameQueryService.findPermanentById(gameData, selfId);
            if (self != null) {
                permanentCounterSupport.placeCounterOnPermanent(gameData, entry, self, e.counterType(), chosenValue);
            }
            return;
        }

        if (maxPotentialX(gameData, controllerId) <= 0) {
            gameLogService.append(gameData, GameLog.text(playerName + " has no mana to pay for " + cardName + "'s ability."));
            log.info("Game {} - {} has no mana for {}'s pay-X ability", gameData.id, playerName, cardName);
            return;
        }
        beginXPrompt(gameData, controllerId, cardName, e.useTriggeringPermanent());
    }

    private void beginXPrompt(GameData gameData, UUID controllerId, String cardName,
                               boolean useTriggeringPermanent) {
        int maxX = maxPotentialX(gameData, controllerId);
        String destination = useTriggeringPermanent ? "that creature" : "it";
        String prompt = "Pay {X} for " + cardName + "? Put X counters on " + destination + ".";
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(controllerId, maxX, prompt, cardName, true));
    }

    private int maxPotentialX(GameData gameData, UUID controllerId) {
        int untappedSources = potentialManaService.buildVirtualManaPool(gameData, controllerId).getTotal()
                - gameData.playerManaPools.get(controllerId).getTotal();
        return payableFromPool(gameData.playerManaPools.get(controllerId)) + untappedSources;
    }

    /**
     * Generic-payable mana in the pool right now. The {X} is paid while the ability resolves, so
     * mana that only can't be spent to cast spells (Powerstone, Thran Turbine) pays it too.
     */
    private static int payableFromPool(ManaPool pool) {
        return new ManaCost("{X}").calculateMaxXForResolutionCost(pool);
    }
}
