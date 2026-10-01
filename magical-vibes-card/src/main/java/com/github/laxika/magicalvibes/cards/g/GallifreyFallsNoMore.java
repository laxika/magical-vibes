package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/** Gallifrey Falls // No More, a split spell with fuse. */
@CardRegistration(set = "WHO", collectorNumber = "131")
@CardRegistration(set = "WHO", collectorNumber = "736")
public class GallifreyFallsNoMore extends Card {

    public GallifreyFallsNoMore() {
        var gallifreyFalls = MassDamageEffect.exilingDamageToEachCreature(4);
        var noMore = new PhaseOutEffect(PhaseOutSubject.TARGET);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Gallifrey Falls — Gallifrey Falls deals 4 damage to each creature. If a creature dealt damage this way would die this turn, exile it instead",
                        gallifreyFalls
                ).withManaCost("{4}{R}{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "No More — Any number of target creatures you control phase out",
                        List.of(noMore), TargetFilters.creatureYouControl(), null, 0, 99, false, null
                ).withManaCost("{2}{W}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Gallifrey Falls and then No More",
                        List.of(gallifreyFalls, noMore), TargetFilters.creatureYouControl(), null,
                        0, 99, false, null
                ).withManaCost("{2}{W}{4}{R}{R}")
        )));
    }
}
