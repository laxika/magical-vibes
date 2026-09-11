package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZNR", collectorNumber = "61")
public class InscriptionOfInsight extends Card {

    public InscriptionOfInsight() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}{U}{U}"));

        var anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");
        var cardsInTargetPlayerHand = new CardsInHand(CountScope.TARGET_PLAYER);

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMoreWhen(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return up to two target creatures to their owners' hands",
                        List.of(ReturnToHandEffect.target()), TargetFilters.creature(), null,
                        0, 2, false, null),
                new ChooseOneEffect.ChooseOneOption(
                        "Scry 2, then draw two cards",
                        List.of(new ScryEffect(2), new DrawCardEffect(2))),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player creates an X/X blue Illusion creature token, where X is the number of cards in their hand",
                        new CreateTokenForTargetPlayerEffect(new CreateTokenEffect(
                                "Illusion", cardsInTargetPlayerHand, cardsInTargetPlayerHand,
                                CardColor.BLUE, List.of(CardSubtype.ILLUSION), Set.of(), Set.of())),
                        anyPlayer)
        ), new Kicked()));
    }
}
