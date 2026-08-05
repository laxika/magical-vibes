package com.github.laxika.magicalvibes.model.effect;

/**
 * Forgotten Lore: "Target opponent chooses a card in your graveyard. You may pay {G}. If you do,
 * repeat this process except that opponent can't choose a card already chosen for Forgotten Lore.
 * Then put the last chosen card into your hand."
 *
 * <p>Resolved by {@code ForgottenLoreEffectHandler}, which alternates a mandatory graveyard pick by
 * the targeted opponent with a "pay {G}" list pick by the controller until the controller declines,
 * can't pay, or no unchosen card remains.
 */
public record ForgottenLoreEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
