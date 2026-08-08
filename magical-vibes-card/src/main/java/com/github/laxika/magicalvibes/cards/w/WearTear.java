package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Wear // Tear — a split card with fuse.
 * <p>
 * Wear {1}{R}: Destroy target artifact.
 * Tear {W}: Destroy target enchantment.
 * Fuse {1}{R}{W}: cast both halves as one spell, resolving Wear and then Tear (CR 702.102d).
 * <p>
 * The caster chooses which half (or both) before the spell goes on the stack (CR 708.3), so the
 * three choices are modelled as the modes of one {@link ChooseOneEffect}, each carrying its own
 * total mana cost — the fuse mode's is the two halves combined (CR 702.102c). The fuse mode
 * declares one filter per half so the two targets are chosen independently; shared targets are
 * allowed because fusing both halves onto one artifact enchantment is legal.
 */
@CardRegistration(set = "DGM", collectorNumber = "135")
public class WearTear extends Card {

    public WearTear() {
        setAllowSharedTargets(true);

        TargetFilter artifact = TargetFilters.artifact();
        TargetFilter enchantment = TargetFilters.enchantment();
        CardEffect wear = new DestroyTargetPermanentEffect();
        CardEffect tear = new DestroyTargetPermanentEffect();

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Wear — Destroy target artifact",
                        wear,
                        artifact
                ).withManaCost("{1}{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Tear — Destroy target enchantment",
                        tear,
                        enchantment
                ).withManaCost("{W}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Wear and then Tear",
                        List.of(wear, tear),
                        List.of(artifact, enchantment)
                ).withManaCost("{1}{R}{W}")
        )));
    }
}
