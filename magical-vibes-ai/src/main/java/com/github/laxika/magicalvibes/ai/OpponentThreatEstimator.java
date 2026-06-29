package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;

/**
 * Lightweight estimator for opponent's potential combat tricks based on
 * open mana and hand size. Used to apply pessimism to combat evaluations
 * so the AI "plays around" potential tricks instead of assuming the
 * opponent's hand is blank.
 * <p>
 * Does <em>not</em> predict exact cards — only estimates broad categories
 * like "opponent can likely cast a 2-mana combat trick" based on open mana
 * colors and total available mana.
 */
public class OpponentThreatEstimator {

    /**
     * Estimated threat from an opponent's hidden hand and open mana.
     *
     * @param trickProbability   probability the opponent holds a relevant combat trick (0.0–0.5)
     * @param estimatedPumpBoost estimated maximum +X/+X a pump spell could provide
     */
    public record ThreatEstimate(double trickProbability, int estimatedPumpBoost) {
        public static final ThreatEstimate NONE = new ThreatEstimate(0, 0);

        /** Returns true when there is a non-zero chance of a meaningful combat trick. */
        public boolean hasThreat() {
            return trickProbability > 0 && estimatedPumpBoost > 0;
        }
    }

    /**
     * Estimates the probability and magnitude of combat tricks the opponent
     * might have.
     * <p>
     * Probability scales with hand size (more cards → more likely to hold a trick).
     * Pump magnitude is derived from the opponent's available mana colors:
     * <ul>
     *   <li>Green — strongest pump (Giant Growth +3/+3 at 1 mana, Titanic Growth +4/+4 at 2)</li>
     *   <li>White — moderate pump / protection (Moment of Triumph +2/+2 at 1 mana)</li>
     *   <li>Red — power-focused tricks (Infuriate +3/+2 at 1 mana, Brute Strength +3/+1 at 2)</li>
     *   <li>Black — small pump (Vampire's Bite +3/+0 at 1 mana, Unlikely Aid +2/+0 at 2)</li>
     *   <li>Colorless-only — conservative generic estimate</li>
     * </ul>
     *
     * @param handSize      number of cards in opponent's hand
     * @param availableMana the opponent's available (potential) mana from untapped sources
     * @return a {@link ThreatEstimate} describing trick probability and estimated pump magnitude
     */
    public static ThreatEstimate estimate(int handSize, ManaPool availableMana) {
        if (handSize <= 0) {
            return ThreatEstimate.NONE;
        }

        int totalMana = availableMana.getTotal();
        if (totalMana <= 0) {
            return ThreatEstimate.NONE;
        }

        // Probability scales with hand size: more cards → more likely to hold a trick.
        // 1 card ≈ 12%, 2 cards ≈ 20%, 3 cards ≈ 28%, 5+ cards ≈ 44%, cap at 50%.
        double trickProbability = Math.min(0.50, 0.04 + handSize * 0.08);

        // Estimate maximum pump boost from available mana colors.
        int estimatedPump = 0;

        boolean hasGreen = availableMana.get(ManaColor.GREEN) > 0;
        boolean hasWhite = availableMana.get(ManaColor.WHITE) > 0;
        boolean hasRed   = availableMana.get(ManaColor.RED) > 0;
        boolean hasBlack  = availableMana.get(ManaColor.BLACK) > 0;

        // Green — strongest pump color
        if (hasGreen) {
            if (totalMana >= 3) {
                estimatedPump = Math.max(estimatedPump, 4);   // Titanic Growth range
            } else if (totalMana >= 1) {
                estimatedPump = Math.max(estimatedPump, 3);   // Giant Growth range
            }
        }

        // White — moderate pump + protection tricks
        if (hasWhite) {
            if (totalMana >= 2) {
                estimatedPump = Math.max(estimatedPump, 2);   // Feat of Resistance range
            } else {
                estimatedPump = Math.max(estimatedPump, 2);   // Moment of Triumph at 1 mana
            }
        }

        // Red — power-focused pump
        if (hasRed) {
            if (totalMana >= 2) {
                estimatedPump = Math.max(estimatedPump, 3);   // Brute Strength range
            } else {
                estimatedPump = Math.max(estimatedPump, 2);   // Infuriate / Rush of Adrenaline
            }
        }

        // Black — smaller pump, mostly power
        if (hasBlack && totalMana >= 1) {
            estimatedPump = Math.max(estimatedPump, 2);       // Vampire's Bite range
        }

        // Colorless-only: conservative generic estimate
        if (estimatedPump == 0) {
            if (totalMana >= 3) {
                estimatedPump = 2;
            } else if (totalMana >= 2) {
                estimatedPump = 1;
            }
            // totalMana == 1 with no relevant color → no pump expected
        }

        if (estimatedPump == 0) {
            return ThreatEstimate.NONE;
        }

        return new ThreatEstimate(trickProbability, estimatedPump);
    }

    private OpponentThreatEstimator() {}
}
