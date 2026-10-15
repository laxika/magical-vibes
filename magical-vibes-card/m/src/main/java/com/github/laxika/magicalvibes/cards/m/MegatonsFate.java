package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GiveEachPlayerRadCounterEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "61")
@CardRegistration(set = "PIP", collectorNumber = "388")
@CardRegistration(set = "PIP", collectorNumber = "589")
@CardRegistration(set = "PIP", collectorNumber = "916")
public class MegatonsFate extends Card {

    public MegatonsFate() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target artifact. Create four Treasure tokens.",
                        List.of(
                                new DestroyTargetPermanentEffect(),
                                CreateTokenEffect.ofTreasureToken(4)
                        ),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsArtifactPredicate(),
                                "Target must be an artifact.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Megaton's Fate deals 8 damage to each creature. Each player gets four rad counters.",
                        List.of(
                                new MassDamageEffect(8),
                                new GiveEachPlayerRadCounterEffect(4)
                        ))
        )));
    }
}
