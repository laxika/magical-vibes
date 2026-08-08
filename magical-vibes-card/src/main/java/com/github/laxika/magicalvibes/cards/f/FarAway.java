package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Far // Away — a split card with fuse.
 * <p>
 * Far {1}{U}: Return target creature to its owner's hand.
 * Away {2}{B}: Target player sacrifices a creature of their choice.
 * Fuse {3}{U}{B}: cast both halves as one spell, resolving Far and then Away (CR 702.102d).
 * <p>
 * The caster chooses which half (or both) before the spell goes on the stack (CR 709.3), so the
 * three choices are modelled as the modes of one {@link ChooseOneEffect}, each carrying its own
 * total mana cost — the fuse mode's is the two halves combined (CR 702.102c). The fuse mode
 * declares one target filter per half so a creature and a player are chosen independently.
 */
@CardRegistration(set = "DGM", collectorNumber = "127")
public class FarAway extends Card {

    public FarAway() {
        TargetFilter creature = TargetFilters.creature();
        TargetFilter anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player.");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Far — Return target creature to its owner's hand",
                        ReturnToHandEffect.target(),
                        creature
                ).withManaCost("{1}{U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Away — Target player sacrifices a creature of their choice",
                        new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                                SacrificeRecipient.TARGET_PLAYER),
                        anyPlayer
                ).withManaCost("{2}{B}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Return target creature to its owner's hand and target player sacrifices a creature of their choice",
                        List.<CardEffect>of(
                                ReturnToHandEffect.target(),
                                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                                        SacrificeRecipient.TARGET_PLAYER)),
                        List.of(creature, anyPlayer)
                ).withManaCost("{3}{U}{B}")
        )));
    }
}
