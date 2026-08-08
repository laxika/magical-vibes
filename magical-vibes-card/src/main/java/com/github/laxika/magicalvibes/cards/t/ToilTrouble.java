package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

/**
 * Toil // Trouble — a split card with fuse.
 * <p>
 * Toil {2}{B}: Target player draws two cards and loses 2 life.
 * Trouble {2}{R}: Trouble deals damage to target player equal to the number of cards in that
 * player's hand.
 * Fuse {4}{B}{R}: cast both halves as one spell, resolving Toil and then Trouble (CR 702.102d).
 * <p>
 * The caster chooses which half (or both) before the spell goes on the stack (CR 709.3), so the
 * three choices are modelled as the modes of one {@link ChooseOneEffect}, each carrying its own
 * total mana cost — the fuse mode's is the two halves combined (CR 702.102c). Toil's draw-and-lose
 * life is one {@link SequenceEffect} so the fuse mode can declare one player filter per half; shared
 * targets are allowed because fusing both halves onto one player is legal, and Toil resolves before
 * Trouble so the drawn cards count toward Trouble's damage.
 */
@CardRegistration(set = "DGM", collectorNumber = "133")
public class ToilTrouble extends Card {

    public ToilTrouble() {
        setAllowSharedTargets(true);

        TargetFilter anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player.");

        CardEffect toil = SequenceEffect.of(
                new DrawCardForTargetPlayerEffect(2),
                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER));
        CardEffect trouble = new DealDamageToPlayersEffect(
                new CardsInHand(CountScope.TARGET_PLAYER), DamageRecipient.TARGET_PLAYER);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Toil — Target player draws two cards and loses 2 life",
                        toil,
                        anyPlayer
                ).withManaCost("{2}{B}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Trouble — Deals damage to target player equal to the number of cards in that player's hand",
                        trouble,
                        anyPlayer
                ).withManaCost("{2}{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Toil and then Trouble",
                        List.of(toil, trouble),
                        List.of(anyPlayer, anyPlayer)
                ).withManaCost("{4}{B}{R}")
        )));
    }
}
