package com.github.laxika.magicalvibes.model.effect;

/**
 * Separate all permanents target player controls into two piles.
 * That player sacrifices all permanents in the pile of their choice.
 * (e.g. Liliana of the Veil ultimate)
 *
 * <p>Flow:
 * <ol>
 *   <li>Controller of the ability is prompted to assign permanents to Pile 1 (multi-permanent choice).
 *       Unselected permanents automatically form Pile 2.</li>
 *   <li>Target player chooses which pile to sacrifice (may-ability choice: Yes = Pile 1, No = Pile 2).</li>
 *   <li>All permanents in the chosen pile are sacrificed.</li>
 * </ol>
 */
public record SeparatePermanentsIntoPilesAndSacrificeEffect() implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
