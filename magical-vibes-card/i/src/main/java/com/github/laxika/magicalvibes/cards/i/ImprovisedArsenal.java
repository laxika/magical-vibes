package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "92")
@CardRegistration(set = "TMT", collectorNumber = "270")
public class ImprovisedArsenal extends Card {

    public ImprovisedArsenal() {
        PermanentCount artifactsYouControl =
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                artifactsYouControl, new Fixed(0), GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}",
                List.of(new CreateTokenCopyOfSourceEffect()),
                "{4}{R}: Create a token that's a copy of this Equipment."));
        addActivatedAbility(new EquipActivatedAbility("{R}"));
    }
}
