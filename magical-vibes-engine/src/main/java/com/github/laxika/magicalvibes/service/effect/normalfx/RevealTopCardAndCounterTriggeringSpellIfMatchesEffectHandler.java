package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.cast.CastingPermissionService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardAndCounterTriggeringSpellIfMatchesEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardAndCounterTriggeringSpellIfMatchesEffectHandler
        implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardAndCounterTriggeringSpellIfMatchesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var reveal = (RevealTopCardAndCounterTriggeringSpellIfMatchesEffect) effect;
        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(entry.getControllerId()) + "'s library is empty."));
            return;
        }

        Card topCard = library.getFirst();
        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(entry.getControllerId()) + " reveals ")
                .card(topCard)
                .text(" from the top of their library.")
                .build());

        UUID triggeringCardId = entry.getTriggeringCardId();
        if (triggeringCardId == null) {
            return;
        }

        // A spell that was already countered or removed in response is no longer on the stack, but the
        // rest of the ability still acts on it as it last existed (only the counter itself is skipped).
        StackEntry liveSpell = counterSupport.findCounterTargetIgnoringCounterability(
                gameData, triggeringCardId, entry);
        StackEntry triggeringSpell = liveSpell != null ? liveSpell : entry.getTriggeringSpellSnapshot();
        if (triggeringSpell == null || !matches(reveal, topCard, triggeringSpell)) {
            return;
        }

        StackEntry counterTarget = liveSpell == null
                ? null : counterSupport.findCounterTarget(gameData, triggeringCardId, entry);
        if (counterTarget != null) {
            counterSupport.counterSpell(gameData, entry, counterTarget);
        }

        if (reveal.spellControllerMayCastFree() && !topCard.hasType(CardType.LAND)) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    topCard,
                    triggeringSpell.getControllerId(),
                    List.of(new RevealTopCardMayPlayFreeEffect(
                            LookDestination.TOP_OF_LIBRARY, false, entry.getControllerId())),
                    "Cast " + topCard.getName() + " without paying its mana cost?"));
            log.info("Game {} - {} may cast {} without paying its mana cost",
                    gameData.id, gameData.playerIdToName.get(triggeringSpell.getControllerId()),
                    topCard.getName());
        }
    }

    private boolean matches(RevealTopCardAndCounterTriggeringSpellIfMatchesEffect reveal, Card topCard,
                            StackEntry triggeringSpell) {
        // A face-down spell has only its face-down characteristics (CR 708.4).
        Card spellCard = CastingPermissionService.spellAsEffectsSeeIt(triggeringSpell).getCard();
        return switch (reveal.match()) {
            case SAME_MANA_VALUE -> {
                int spellManaValue = triggeringSpell.isCastFaceDown() ? 0 : spellCard.getManaValue()
                        + triggeringSpell.getXValue() * (spellCard.getParsedManaCost() == null
                        ? 0 : spellCard.getParsedManaCost().getXSymbolCount());
                yield topCard.getManaValue() == spellManaValue;
            }
            case SHARES_CARD_TYPE -> {
                Set<CardType> spellTypes = EnumSet.of(spellCard.getType());
                spellTypes.addAll(spellCard.getAdditionalTypes());
                yield counterSupport.sharesCardType(topCard, spellTypes);
            }
        };
    }
}
