package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryToHandAndRegisterExileAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "3")
public class ThoughtweftsCall extends Card {

    public ThoughtweftsCall() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Seek a Kithkin card",
                        new SeekLibraryToHandAndRegisterExileAtNextEndStepEffect(
                                new CardSubtypePredicate(CardSubtype.KITHKIN))),
                new ChooseOneEffect.ChooseOneOption(
                        "Creature cards in your hand perpetually get +1/+1",
                        new PerpetuallyBoostMatchingHandCardsEffect(
                                new CardTypePredicate(CardType.CREATURE), 1, 1))
        )));
    }
}
