package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesExceptThoseSharingConvokeCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "19")
@CardRegistration(set = "WHO", collectorNumber = "339")
@CardRegistration(set = "WHO", collectorNumber = "624")
@CardRegistration(set = "WHO", collectorNumber = "930")
public class EverythingComesToDust extends Card {

    public EverythingComesToDust() {
        // Exile all artifacts, creatures, and enchantments except creatures that share a creature type with a
        // creature that convoked this spell: one instruction, so everything leaves the battlefield at once.
        addEffect(EffectSlot.SPELL, new ExileCreaturesExceptThoseSharingConvokeCreatureTypeEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(), new PermanentIsEnchantmentPredicate()))));
    }
}
