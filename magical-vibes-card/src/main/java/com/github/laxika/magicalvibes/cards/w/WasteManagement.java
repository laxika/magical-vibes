package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "40")
@CardRegistration(set = "NCC", collectorNumber = "141")
public class WasteManagement extends Card {

    public WasteManagement() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{3}{B}"));

        CardTypePredicate creatureCard = new CardTypePredicate(CardType.CREATURE);
        targetWhenKicked(
                new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.ALL_GRAVEYARDS),
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"),
                0, 2, 1, 1)
                .addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                        new Kicked(),
                        new ExileGraveyardCardsEffect(
                                2, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD,
                                null, null, false, false, false, creatureCard, false, true),
                        new ExileGraveyardCardsEffect(
                                0, GraveyardExileScope.TARGET_PLAYER_ALL_MATCHING,
                                null, null, false, false, false, creatureCard, false)));

        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new EventValue(), "Rogue", 2, 2, CardColor.BLACK,
                List.of(CardSubtype.ROGUE), Set.of(), Set.of()));
    }
}
