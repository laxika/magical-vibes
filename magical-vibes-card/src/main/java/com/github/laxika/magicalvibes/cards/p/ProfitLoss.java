package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/**
 * Profit // Loss — a split card with fuse.
 * <p>
 * Profit {1}{W}: Creatures you control get +1/+1 until end of turn.
 * Loss {2}{B}: Creatures your opponents control get -1/-1 until end of turn.
 * Fuse {3}{W}{B}: cast both halves as one spell, resolving Profit and then Loss (CR 702.102d).
 * <p>
 * The caster chooses which half (or both) before the spell goes on the stack (CR 709.3), so the
 * three choices are modelled as the modes of one {@link ChooseOneEffect}, each carrying its own
 * total mana cost — the fuse mode's is the two halves combined (CR 702.102c). Both halves are
 * untargeted mass pumps.
 */
@CardRegistration(set = "DGM", collectorNumber = "130")
public class ProfitLoss extends Card {

    public ProfitLoss() {
        CardEffect profit = new BoostAllOwnCreaturesEffect(1, 1);
        CardEffect loss = new BoostAllCreaturesEffect(-1, -1,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Profit — Creatures you control get +1/+1 until end of turn",
                        profit
                ).withManaCost("{1}{W}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Loss — Creatures your opponents control get -1/-1 until end of turn",
                        loss
                ).withManaCost("{2}{B}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Profit and then Loss",
                        List.of(profit, loss)
                ).withManaCost("{3}{W}{B}")
        )));
    }
}
