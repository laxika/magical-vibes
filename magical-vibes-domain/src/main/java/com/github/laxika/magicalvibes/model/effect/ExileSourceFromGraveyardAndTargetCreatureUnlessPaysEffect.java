package com.github.laxika.magicalvibes.model.effect;

/**
 * Punisher exile for a graveyard-activated ability: exile the source card from its owner's
 * graveyard <em>and</em> the target creature, unless that creature's controller pays
 * {@code manaCost}. The decision belongs to the target creature's controller — paying stops the
 * whole effect, so neither the source card nor the creature is exiled. Used by Carrionette
 * ({@code {2}{B}{B}: Exile this card and target creature unless that creature's controller pays
 * {2}.}).
 *
 * @param manaCost what the target creature's controller may pay to stop the exile
 */
public record ExileSourceFromGraveyardAndTargetCreatureUnlessPaysEffect(String manaCost)
        implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
