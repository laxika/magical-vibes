package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MAT", collectorNumber = "49")
@CardRegistration(set = "MAT", collectorNumber = "99")
@CardRegistration(set = "MAT", collectorNumber = "149")
@CardRegistration(set = "MAT", collectorNumber = "184")
@CardRegistration(set = "MB2", collectorNumber = "255")
public class KarnLegacyReforged extends Card {

    public KarnLegacyReforged() {
        PermanentIsArtifactPredicate artifact = new PermanentIsArtifactPredicate();
        addStaticEffect(new SetPowerToughnessToAmountEffect(
                new GreatestManaValueAmongControlled(artifact),
                new GreatestManaValueAmongControlled(artifact)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new AwardRestrictedManaEffect(
                ManaColor.COLORLESS,
                new PermanentCount(artifact, CountScope.CONTROLLER),
                new ManaRestriction.Powerstone(true)));
    }
}
