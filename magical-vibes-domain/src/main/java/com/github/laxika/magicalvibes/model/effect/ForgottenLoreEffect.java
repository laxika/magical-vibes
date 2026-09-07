package com.github.laxika.magicalvibes.model.effect;

/**
 * A Forgotten Lore-style effect: "Target opponent chooses a card in your graveyard. You may pay
 * the configured mana cost. If you do, repeat this process except that opponent can't choose a card
 * already chosen for this spell. Then put the last chosen card into your hand."
 *
 * <p>Resolved by {@code ForgottenLoreEffectHandler}, which alternates a mandatory graveyard pick by
 * the targeted opponent with a payment list pick by the controller until the controller declines,
 * can't pay, or no unchosen card remains.
 */
public record ForgottenLoreEffect(String repeatManaCost) implements CardEffect {

    public ForgottenLoreEffect() {
        this("{G}");
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
