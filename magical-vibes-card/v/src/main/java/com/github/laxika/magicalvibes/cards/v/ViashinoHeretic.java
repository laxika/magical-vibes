package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "95")
public class ViashinoHeretic extends Card {

    public ViashinoHeretic() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(SequenceEffect.of(
                        new DestroyTargetPermanentEffect(),
                        new DealDamageToPlayersEffect(new TargetManaValue(), Subjects.controllerOf(Subjects.target())))),
                "{1}{R}, {T}: Destroy target artifact. This creature deals damage to that artifact's controller equal to the artifact's mana value.",
                TargetFilters.artifact()));
    }
}
