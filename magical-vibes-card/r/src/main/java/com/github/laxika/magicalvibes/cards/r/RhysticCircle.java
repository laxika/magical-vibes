package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "19")
public class RhysticCircle extends Card {

    public RhysticCircle() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(UnlessPaysEffect.unlessPays(Subjects.eachPlayer(), "{1}",
                        PreventDamageFromChosenSourceEffect.nextDamageToYou())),
                "Any player may pay {1}. If no one does, the next time a source of your choice would deal damage to you this turn, prevent that damage."
        ));
    }
}
