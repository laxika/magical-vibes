package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "170")
public class PickYourPoison extends Card {

    public PickYourPoison() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Each opponent sacrifices an artifact of their choice",
                        new SacrificePermanentsEffect(1, new PermanentIsArtifactPredicate(),
                                SacrificeRecipient.EACH_OPPONENT)),
                new ChooseOneEffect.ChooseOneOption(
                        "Each opponent sacrifices an enchantment of their choice",
                        new SacrificePermanentsEffect(1, new PermanentIsEnchantmentPredicate(),
                                SacrificeRecipient.EACH_OPPONENT)),
                new ChooseOneEffect.ChooseOneOption(
                        "Each opponent sacrifices a creature with flying of their choice",
                        new SacrificePermanentsEffect(1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasKeywordPredicate(Keyword.FLYING))),
                                SacrificeRecipient.EACH_OPPONENT))
        )));
    }
}
