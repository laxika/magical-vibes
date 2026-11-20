package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPermanentsTargetControlsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject.OtherCreaturesSharing.SharedQuality;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "152")
public class WojekEmbermage extends Card {

    public WojekEmbermage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(SequenceEffect.simultaneous(new DealDamageToTargetCreatureEffect(1),
                        new DealDamageToPermanentsTargetControlsEffect(1,
                        Subjects.otherCreaturesSharing(Subjects.target(), SharedQuality.COLOR)))),
                "{T}: This creature deals 1 damage to target creature and each other creature that shares a color with it.",
                TargetFilters.creature()
        ));
    }
}
