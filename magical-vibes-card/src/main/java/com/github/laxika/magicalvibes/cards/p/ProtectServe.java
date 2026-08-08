package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Protect // Serve — a split card with fuse.
 * <p>
 * Protect {2}{W}: Target creature gets +2/+4 until end of turn.
 * Serve {1}{U}: Target creature gets -6/-0 until end of turn.
 * Fuse {3}{W}{U}: cast both halves as one spell, resolving Protect and then Serve (CR 702.102d).
 * <p>
 * The caster chooses which half (or both) before the spell goes on the stack (CR 709.3), so the
 * three choices are modelled as the modes of one {@link ChooseOneEffect}, each carrying its own
 * total mana cost — the fuse mode's is the two halves combined (CR 702.102c). The fuse mode
 * declares one creature filter per half so the two targets are chosen independently; shared
 * targets are allowed because fusing both halves onto one creature is legal.
 */
@CardRegistration(set = "DGM", collectorNumber = "131")
public class ProtectServe extends Card {

    public ProtectServe() {
        setAllowSharedTargets(true);

        TargetFilter creature = TargetFilters.creature();
        CardEffect protect = new BoostTargetCreatureEffect(2, 4);
        CardEffect serve = new BoostTargetCreatureEffect(-6, 0);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Protect — Target creature gets +2/+4 until end of turn",
                        protect,
                        creature
                ).withManaCost("{2}{W}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Serve — Target creature gets -6/-0 until end of turn",
                        serve,
                        creature
                ).withManaCost("{1}{U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Protect and then Serve",
                        List.of(protect, serve),
                        List.of(creature, creature)
                ).withManaCost("{3}{W}{U}")
        )));
    }
}
