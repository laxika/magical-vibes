package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PutRandomCreatureFromRandomGraveyardOntoBattlefieldAndDealManaValueDamageEffect;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class PutRandomCreatureFromRandomGraveyardOntoBattlefieldAndDealManaValueDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final CloneService cloneService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutRandomCreatureFromRandomGraveyardOntoBattlefieldAndDealManaValueDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        entry.setEventValue(0);

        List<UUID> eligibleGraveyardOwners = gameData.orderedPlayerIds.stream()
                .filter(playerId -> {
                    List<Card> graveyard = gameData.playerGraveyards.get(playerId);
                    return graveyard != null && graveyard.stream()
                            .anyMatch(card -> card.hasType(CardType.CREATURE));
                })
                .toList();
        if (eligibleGraveyardOwners.isEmpty()) {
            return;
        }

        UUID graveyardOwnerId = eligibleGraveyardOwners.get(
                ThreadLocalRandom.current().nextInt(eligibleGraveyardOwners.size()));
        List<Card> creatureCards = gameData.playerGraveyards.get(graveyardOwnerId).stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .toList();
        Card card = creatureCards.get(ThreadLocalRandom.current().nextInt(creatureCards.size()));
        if (graveyardReturnSupport.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
        Permanent permanent = new Permanent(card);
        permanent.setEnteredFromGraveyardOwnerId(graveyardOwnerId);
        entry.setEventValue(card.getManaValue());
        entry.setTargetId(permanent.getId());
        entry.rememberLastKnownPermanentCard(permanent.getId(), card);
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        entry.insertEffectsToResolve(effectIndex + 1,
                List.of(new DealDamageToPlayersEffect(new TargetManaValue(), DamageRecipient.CONTROLLER)));

        if (cloneService.prepareCloneReplacementEffect(
                gameData, entry.getControllerId(), card, null)) {
            gameData.cloneOperation.preparedPermanent = permanent;
            if (!entry.getControllerId().equals(graveyardOwnerId)) {
                graveyardReturnSupport.trackStolenCreature(
                        gameData, permanent.getId(), entry.getControllerId(), graveyardOwnerId);
            }
            return;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        battlefieldEntryService.putPermanentOntoBattlefield(
                gameData, entry.getControllerId(), permanent, enterTappedTypes);
        if (!entry.getControllerId().equals(graveyardOwnerId)) {
            graveyardReturnSupport.trackStolenCreature(
                    gameData, permanent.getId(), entry.getControllerId(), graveyardOwnerId);
        }

        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(entry.getControllerId()) + " puts ")
                .card(card)
                .text(" onto the battlefield under their control.")
                .build());
        graveyardReturnSupport.handleCreatureEtbAndLegendRule(
                gameData, entry.getControllerId(), permanent, card);
    }
}
