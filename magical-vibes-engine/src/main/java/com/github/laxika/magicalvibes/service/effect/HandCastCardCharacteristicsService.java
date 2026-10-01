package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.CastCreatureCardsAsNamedCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdventureToCreatureCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.GrantOffspringToCreatureSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.List;
import java.util.UUID;

/** Resolves static effects that change the characteristics used to cast a card from hand. */
public final class HandCastCardCharacteristicsService {

    private HandCastCardCharacteristicsService() {
    }

    public static Card effectiveCard(GameData gameData, UUID playerId, Card card,
                                     GameQueryService gameQueryService) {
        if (card == null || !card.hasType(CardType.CREATURE)) {
            return card;
        }

        Card namedCopyFace = null;
        boolean grantsAdventure = false;
        String offspringCost = null;
        for (Permanent source : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
            boolean hasNamedCopyEffect = gameQueryService.hasActiveStaticEffect(
                    gameData, source, CastCreatureCardsAsNamedCardEffect.class);
            boolean hasAdventureEffect = gameQueryService.hasActiveStaticEffect(
                    gameData, source, GrantAdventureToCreatureCardsInHandEffect.class);
            boolean hasOffspringEffect = gameQueryService.hasActiveStaticEffect(
                    gameData, source, GrantOffspringToCreatureSpellsEffect.class);
            for (var effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (hasNamedCopyEffect && effect instanceof CastCreatureCardsAsNamedCardEffect castAs
                        && namedCopyFace == null) {
                    namedCopyFace = castAs.card();
                }
                if (hasAdventureEffect && effect instanceof GrantAdventureToCreatureCardsInHandEffect) {
                    grantsAdventure = true;
                }
                if (hasOffspringEffect && effect instanceof GrantOffspringToCreatureSpellsEffect offspring
                        && offspringCost == null) {
                    offspringCost = offspring.offspringCost();
                }
            }
        }

        Card effective = namedCopyFace == null ? card : card.createRuntimeCopyWithFace(namedCopyFace);
        if (grantsAdventure && effective.getCastingOption(AdventureCast.class).isEmpty()) {
            Card copy = effective.createRuntimeCopy();
            copy.setBackFaceCard(fetchHerbsFace());
            copy.addCastingOption(new AdventureCast("{1}{G}"));
            effective = copy;
        }
        if (offspringCost != null && effective.getEffects(EffectSlot.STATIC).stream()
                .noneMatch(KickerEffect.class::isInstance)) {
            Card copy = effective.createRuntimeCopy();
            copy.addEffect(EffectSlot.STATIC, new KickerEffect(offspringCost));
            copy.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Kicked(),
                    new CreateTokenCopyOfSourceEffect(false, 1, null, null, false, 1, 1)));
            effective = copy;
        }
        if (effective != card) {
            effective.freeze();
        }
        return effective;
    }

    public static Card effectiveCardIfInHand(GameData gameData, UUID playerId, Card card,
                                             GameQueryService gameQueryService) {
        if (card == null || playerId == null
                || playerId.equals(gameData.commandCastPlayerId)
                || gameData.playerHands.getOrDefault(playerId, List.of()).stream()
                .noneMatch(handCard -> handCard.getId().equals(card.getId()))) {
            return card;
        }
        return effectiveCard(gameData, playerId, card, gameQueryService);
    }

    private static Card fetchHerbsFace() {
        Card fetchHerbs = new Card();
        fetchHerbs.setName("Fetch Herbs");
        fetchHerbs.setType(CardType.SORCERY);
        fetchHerbs.setManaCost("{1}{G}");
        fetchHerbs.setColor(CardColor.GREEN);
        fetchHerbs.setColors(List.of(CardColor.GREEN));
        fetchHerbs.setColorIdentity(List.of(CardColor.GREEN));
        fetchHerbs.setCardText("You gain 2 life.");
        fetchHerbs.addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
        fetchHerbs.freeze();
        return fetchHerbs;
    }
}
