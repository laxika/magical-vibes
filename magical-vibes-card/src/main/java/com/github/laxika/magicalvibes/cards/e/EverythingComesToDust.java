package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesExceptThoseSharingConvokeCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "WHO", collectorNumber = "19")
@CardRegistration(set = "WHO", collectorNumber = "339")
public class EverythingComesToDust extends Card {

    public EverythingComesToDust() {
        addEffect(EffectSlot.SPELL, new ExileCreaturesExceptThoseSharingConvokeCreatureTypeEffect());
        addEffect(EffectSlot.SPELL, new ExileAllPermanentsEffect(new PermanentIsArtifactPredicate()));
        addEffect(EffectSlot.SPELL, new ExileAllPermanentsEffect(new PermanentIsEnchantmentPredicate()));
    }
}
