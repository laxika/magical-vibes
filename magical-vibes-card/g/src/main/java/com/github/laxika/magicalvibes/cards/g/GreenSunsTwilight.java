package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsRevealTwoTypesToHandThenRestEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "169")
public class GreenSunsTwilight extends Card {

    public GreenSunsTwilight() {
        DynamicAmount lookCount = new Sum(new XValue(), new Fixed(1));
        CardEffect toHand = new LookAtTopCardsRevealTwoTypesToHandThenRestEffect(
                lookCount, CardType.CREATURE, CardType.LAND, List.of(),
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, true, LibrarySearchDestination.HAND);
        CardEffect toBattlefield = LookAtTopCardsRevealTwoTypesToHandThenRestEffect
                .creatureAndLandToBattlefieldRestOnBottomRandom(lookCount);

        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new SpellXAtLeast(5)), toHand));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new SpellXAtLeast(5), new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Put the chosen cards onto the battlefield", toBattlefield),
                        new ChooseOneEffect.ChooseOneOption(
                                "Put the chosen cards into your hand", toHand)))));
    }
}
