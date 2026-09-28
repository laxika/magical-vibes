package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "53")
@CardRegistration(set = "CMM", collectorNumber = "470")
@CardRegistration(set = "C15", collectorNumber = "7")
public class RighteousConfluence extends Card {

    public RighteousConfluence() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.withRepeatedModes(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 2/2 white Knight creature token with vigilance.",
                        new CreateTokenEffect(
                                1, "Knight", 2, 2, CardColor.WHITE,
                                List.of(CardSubtype.KNIGHT), Set.of(Keyword.VIGILANCE), Set.of())),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target enchantment.",
                        new ExileTargetPermanentEffect(), TargetFilters.enchantment()),
                new ChooseOneEffect.ChooseOneOption(
                        "You gain 5 life.",
                        new GainLifeEffect(5))
        ), 3));
    }
}
