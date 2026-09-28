package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardPowerToughnessModifier;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTargetExiledCardIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPutSelectedCardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Viconia's fresh duplicates of cards exiled with her. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfTargetExiledCardIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfTargetExiledCardIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var conjure = (ConjureDuplicateOfTargetExiledCardIntoHandEffect) effect;
        for (UUID targetId : entry.targetsForEffect(effect)) {
            ExiledCardEntry exiled = gameData.findExiledCard(targetId);
            if (exiled == null) {
                continue;
            }

            Card copy = exiled.card().createCardCopy();
            copy.setOwnerId(entry.getControllerId());
            copy.freeze();
            applyPerpetualChanges(gameData, copy, conjure);
            gameData.addCardToHand(entry.getControllerId(), copy);
            gameLogService.append(gameData, GameLog.cardThen(copy, " is conjured into "
                    + gameData.playerIdToName.get(entry.getControllerId()) + "'s hand."));

            if (conjure.mayPutOntoBattlefieldManaValueAtMost() >= 0) {
                gameData.queueMayAbilityForPlayer(
                        entry.getCard(),
                        entry.getControllerId(),
                        new MayEffect(
                                new MayPutSelectedCardOntoBattlefieldEffect(
                                        conjure.mayPutOntoBattlefieldManaValueAtMost(),
                                        new CardTypePredicate(CardType.CREATURE),
                                        false,
                                        false),
                                "Put the conjured card onto the battlefield?"),
                        copy.getId(),
                        entry.getSourcePermanentId(),
                        entry.getControllerId(),
                        entry.getSourcePermanentSnapshot());
                playerInputService.processNextMayAbility(gameData);
            }
        }
    }

    private void applyPerpetualChanges(GameData gameData, Card copy,
                                        ConjureDuplicateOfTargetExiledCardIntoHandEffect effect) {
        if (effect.anyColorMana()) {
            gameData.perpetualAnyColorManaForCastCardIds.add(copy.getId());
        }
        if (effect.powerBoost() != 0 || effect.toughnessBoost() != 0) {
            gameData.perpetualCardPowerToughnessModifiers.merge(
                    copy.getId(),
                    new CardPowerToughnessModifier(effect.powerBoost(), effect.toughnessBoost()),
                    (oldValue, newValue) -> new CardPowerToughnessModifier(
                            oldValue.power() + newValue.power(), oldValue.toughness() + newValue.toughness()));
        }
        if (!effect.keywords().isEmpty()) {
            gameData.perpetualCardKeywords.merge(copy.getId(), Set.copyOf(effect.keywords()),
                    (existing, added) -> {
                        Set<Keyword> merged = EnumSet.noneOf(Keyword.class);
                        merged.addAll(existing);
                        merged.addAll(added);
                        return Set.copyOf(merged);
                    });
        }
        if (effect.drainOnEnter()) {
            addPerpetualEnterTrigger(gameData, copy.getId());
        }
    }

    private void addPerpetualEnterTrigger(GameData gameData, UUID cardId) {
        CardEffect drain = SequenceEffect.of(
                new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(2));
        gameData.perpetualTriggeredAbilityGrants.compute(cardId, (ignored, existing) -> {
            java.util.Map<EffectSlot, List<CardEffect>> updated = new java.util.EnumMap<>(EffectSlot.class);
            if (existing != null) {
                existing.forEach((slot, effects) -> updated.put(slot, new ArrayList<>(effects)));
            }
            updated.computeIfAbsent(EffectSlot.ON_ENTER_BATTLEFIELD, ignoredSlot -> new ArrayList<>())
                    .add(drain);
            updated.replaceAll((slot, effects) -> List.copyOf(effects));
            return java.util.Map.copyOf(updated);
        });
    }
}
