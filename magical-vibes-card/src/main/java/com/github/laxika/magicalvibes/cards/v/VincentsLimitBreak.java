package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TieredManaCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "126")
public class VincentsLimitBreak extends Card {

    public VincentsLimitBreak() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Galian Beast — {0} — 3/2",
                        List.of(
                                new SetBasePowerToughnessEffect(3, 2),
                                new GrantEffectToTargetUntilEndOfTurnEffect(
                                        EffectSlot.ON_DEATH,
                                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(true))),
                        TargetFilters.creatureYouControl()),
                new ChooseOneEffect.ChooseOneOption(
                        "Death Gigas — {1} — 5/2",
                        List.of(
                                new SetBasePowerToughnessEffect(5, 2),
                                new GrantEffectToTargetUntilEndOfTurnEffect(
                                        EffectSlot.ON_DEATH,
                                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(true))),
                        TargetFilters.creatureYouControl()),
                new ChooseOneEffect.ChooseOneOption(
                        "Hellmasker — {3} — 7/2",
                        List.of(
                                new SetBasePowerToughnessEffect(7, 2),
                                new GrantEffectToTargetUntilEndOfTurnEffect(
                                        EffectSlot.ON_DEATH,
                                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(true))),
                        TargetFilters.creatureYouControl())
        )));
        addEffect(EffectSlot.SPELL, new TieredManaCost(List.of("", "{1}", "{3}")));
    }
}
