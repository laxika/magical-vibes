package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.CastingCost;
import com.github.laxika.magicalvibes.model.GrantedAlternateCast;

import java.util.List;
import java.util.Objects;

/**
 * "[They] perpetually gain 'You may pay {R} rather than pay this spell's mana cost'", "blue creature cards in your
 * hand perpetually gain 'Evoke—Exile a blue card from your hand'": the cards {@code cards} names get the alternative
 * cost (CR 118.9) for as long as they are those cards (digital only), in the one store
 * {@code GameData.perpetualAlternateCasts}. An evoke cost also has its triggered half, "when this permanent enters, if
 * its evoke cost was paid, its controller sacrifices it" (CR 702.74a), which a cast for it adds.
 *
 * @param cards the cards that gain it; a hand's or library's are named in no log (CR 400.2)
 * @param grant the alternative cost, and whether it is evoke
 */
public record PerpetuallyGrantAlternateCastEffect(CardSubject cards, GrantedAlternateCast grant) implements CardEffect {

    public PerpetuallyGrantAlternateCastEffect {
        Objects.requireNonNull(cards, "cards");
        Objects.requireNonNull(grant, "grant");
    }

    /** "Perpetually gain 'Evoke—[costs]'". */
    public static PerpetuallyGrantAlternateCastEffect evoke(CardSubject cards, CastingCost... costs) {
        return new PerpetuallyGrantAlternateCastEffect(cards,
                new GrantedAlternateCast(new AlternateHandCast(List.of(costs)), true));
    }

    /** "Perpetually gain 'You may pay [costs] rather than pay this spell's mana cost'". */
    public static PerpetuallyGrantAlternateCastEffect ratherThanManaCost(CardSubject cards, CastingCost... costs) {
        return new PerpetuallyGrantAlternateCastEffect(cards,
                new GrantedAlternateCast(new AlternateHandCast(List.of(costs)), false));
    }

    @Override
    public TargetSpec targetSpec() {
        return cards.targetSpec();
    }
}
