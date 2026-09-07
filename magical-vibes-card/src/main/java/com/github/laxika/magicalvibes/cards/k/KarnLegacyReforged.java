package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MAT", collectorNumber = "49")
public class KarnLegacyReforged extends Card {

    public KarnLegacyReforged() {
        PermanentIsArtifactPredicate artifact = new PermanentIsArtifactPredicate();
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                new GreatestManaValueAmongControlled(artifact),
                new GreatestManaValueAmongControlled(artifact)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new AwardRestrictedManaEffect(
                ManaColor.COLORLESS,
                new PermanentCount(artifact, CountScope.CONTROLLER),
                new ManaRestriction.Powerstone(true)));
    }
}
