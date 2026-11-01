package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "168")
public class WeaponsManufacturing extends Card {

    public WeaponsManufacturing() {
        addEffect(EffectSlot.ON_SELF_OR_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD,
                new CreateTokenEffect(
                        CardType.ARTIFACT, 1, "Munitions", 0, 0, null, null,
                        List.of(), Set.of(), Set.of(), false, false,
                        List.of(new TriggeredAbility(Triggers.leavesBattlefield(ObjectRelation.THIS), new DealDamageToAnyTargetEffect(2))),
                        List.of(), false, false, false, 0, Set.of()
                ));
    }
}
