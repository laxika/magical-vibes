package com.github.laxika.magicalvibes.model.effect;

/**
 * Punisher tap: tap the target creature unless its controller pays {@code lifeCost} life. The
 * decision belongs to the target creature's controller — a controller who can pay is prompted via
 * the may-ability system; one who can't (too little life, or life can't change) has the creature
 * tapped immediately. Used by Vectis Dominator ({@code {T}: Tap target creature unless its
 * controller pays 2 life.}).
 *
 * @param lifeCost how much life the target creature's controller may pay to avoid the tap
 */
public record TapTargetCreatureUnlessControllerPaysLifeEffect(int lifeCost) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
