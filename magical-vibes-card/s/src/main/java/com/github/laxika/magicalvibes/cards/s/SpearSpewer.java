package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "117")
public class SpearSpewer extends Card {

    public SpearSpewer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(DealDamageEffect.to(Subjects.eachPlayer(), 1)),
                "{T}: Spear Spewer deals 1 damage to each player."
        ));
    }
}
