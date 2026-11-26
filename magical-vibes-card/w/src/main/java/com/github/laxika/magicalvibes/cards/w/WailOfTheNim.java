package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateCost;
import com.github.laxika.magicalvibes.model.effect.RegenerateAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "81")
public class WailOfTheNim extends Card {

    public WailOfTheNim() {
        addCastingCost(EscalateCost.entwine("{B}"));
        addSpellEffect(ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Regenerate each creature you control",
                        new RegenerateAllOwnCreaturesEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Wail of the Nim deals 1 damage to each creature and each player",
                        DealDamageEffect.toEachCreature(1).andTo(Subjects.eachPlayer()))
        )));
    }
}
