package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardTwoDifferentColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "143")
public class OpenTheOmenpaths extends Card {

    public OpenTheOmenpaths() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Add two mana of any one color and two mana of any other color",
                        new AwardTwoDifferentColorManaEffect(
                                2, ManaSpendRestriction.CREATURE_OR_ENCHANTMENT_SPELL_ONLY)),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +1/+0 until end of turn",
                        new BoostAllOwnCreaturesEffect(1, 0))
        )));
    }
}
