package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect declaring that a card has splice — an optional additional cost paid while casting
 * a spell that meets the quality requirement (CR 702.47).
 *
 * <p>"Splice onto [quality] [cost]" means: as you cast a spell with that quality (e.g. Arcane),
 * you may reveal this card from your hand and pay the splice cost. If you do, this card's effects
 * are added to that spell; the spliced card remains in your hand.
 *
 * <p>A splice cost may also be non-mana: "Tap an untapped white creature you control"
 * (Hundred-Talon Strike, {@code tapCost}) or "Return a blue creature you control to its owner's
 * hand" (Veil of Secrecy, {@code returnCost}). Either way the permanent is chosen when casting the
 * host spell and rides on the {@code spliceCostPermanentIds} list of
 * {@code GameService.playCardWithSplice}, one id per spliced card that has such a cost, in the same
 * order as the spliced cards. Paying this way is a cost, not a {@code {T}} activation cost, so a
 * summoning-sick creature may be tapped. A splice cost has at most one permanent component.
 *
 * <p>{@code exileFromGraveyardCount} covers the third non-mana shape, "Exile four cards from your
 * graveyard" (Horobi's Whisper). The cards are taken from the bottom (oldest) of the caster's
 * graveyard: splice has no client-side choice UI yet, so the engine picks deterministically rather
 * than leaving the cost unpayable. Not enough cards in the graveyard means the cost cannot be paid
 * and the cast is rejected.
 *
 * @param ontoSubtype              the subtype the host spell must have (e.g. {@link CardSubtype#ARCANE})
 * @param cost                     the splice mana cost (e.g. "{2}{R}{R}"); empty when the cost is non-mana
 * @param tapCost                  filter for the untapped permanent that must be tapped, or null when there is none
 * @param returnCost               filter for the permanent that must be returned to its owner's hand, or null
 * @param exileFromGraveyardCount  how many cards must be exiled from the caster's graveyard, or 0 for none
 */
public record SpliceEffect(CardSubtype ontoSubtype, String cost, PermanentPredicate tapCost,
                           PermanentPredicate returnCost, int exileFromGraveyardCount) implements CardEffect {

    public SpliceEffect(CardSubtype ontoSubtype, String cost) {
        this(ontoSubtype, cost, null, null, 0);
    }

    public SpliceEffect(CardSubtype ontoSubtype, String cost, PermanentPredicate tapCost) {
        this(ontoSubtype, cost, tapCost, null, 0);
    }

    /** Splice cost whose only component is returning a matching permanent you control to hand. */
    public static SpliceEffect returning(CardSubtype ontoSubtype, PermanentPredicate returnCost) {
        return new SpliceEffect(ontoSubtype, "", null, returnCost, 0);
    }

    /** Splice cost whose only component is exiling {@code count} cards from your graveyard. */
    public static SpliceEffect exilingGraveyard(CardSubtype ontoSubtype, int count) {
        return new SpliceEffect(ontoSubtype, "", null, null, count);
    }

    /** The permanent-choice component of this splice cost, or null when the cost is mana-only. */
    public PermanentPredicate permanentCost() {
        return tapCost != null ? tapCost : returnCost;
    }
}
