package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "75")
@CardRegistration(set = "FDN", collectorNumber = "207")
public class Slagstorm extends Card {

    public Slagstorm() {
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Slagstorm deals 3 damage to each creature", DealDamageEffect.toEachCreature(3)),
                new ChooseOneEffect.ChooseOneOption("Slagstorm deals 3 damage to each player", DealDamageEffect.to(Subjects.eachPlayer(),
                        3))
        )));
    }
}
