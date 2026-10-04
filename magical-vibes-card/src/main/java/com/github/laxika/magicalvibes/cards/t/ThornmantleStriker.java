package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChosenCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "387")
public class ThornmantleStriker extends Card {

    public ThornmantleStriker() {
        var elfCount = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.ELF), CountScope.CONTROLLER);
        var opponentCreature = TargetFilters.creatureAnOpponentControls();

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Remove X counters from target permanent, where X is the number of Elves you control",
                        new RemoveChosenCountersFromTargetPermanentEffect(elfCount, true),
                        TargetFilters.permanent()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature an opponent controls gets -X/-X until end of turn, where X is the number of Elves you control",
                        new BoostTargetCreatureEffect(new Scaled(elfCount, -1), new Scaled(elfCount, -1),
                                opponentCreature.predicate()),
                        opponentCreature)
        )));
    }
}
