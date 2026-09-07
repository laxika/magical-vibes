package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetOnControllerSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "195")
public class NarsetOfTheAncientWay extends Card {

    public NarsetOfTheAncientWay() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new GainLifeEffect(2),
                        new AwardRestrictedManaOfColorsEffect(
                                List.of(ManaColor.BLUE, ManaColor.RED, ManaColor.WHITE),
                                new ManaRestriction.NoncreatureSpells())
                ),
                "+1: You gain 2 life. Add {U}, {R}, or {W}. Spend this mana only to cast a noncreature spell."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(
                        new DrawCardEffect(1),
                        new MayEffect(
                                new DiscardCardThenEffect(
                                        null,
                                        new DealDamageToTargetCreatureOrPlaneswalkerEffect(
                                                new LastDiscardedCardManaValue()),
                                        "a card",
                                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                                "Discard a card?")
                ),
                "\u22122: Draw a card, then you may discard a card. When you discard a nonland card this way, Narset deals damage equal to that card's mana value to target creature or planeswalker."
        ));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new CreateEmblemEffect(
                        List.of(new DealDamageToAnyTargetOnControllerSpellCastEffect(
                                2,
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                                List.of())),
                        "Whenever you cast a noncreature spell, this emblem deals 2 damage to any target.")),
                "\u22126: You get an emblem with \"Whenever you cast a noncreature spell, this emblem deals 2 damage to any target.\""
        ));
    }
}
