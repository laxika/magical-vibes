package com.github.laxika.magicalvibes.model.effect;

/**
 * "That player copies that spell for each other creature they control that the spell could target. Each copy targets
 * a different one of those creatures" (Mirrorwing Dragon, Zada): the effect of a filtered cast trigger
 * ({@code Triggers.playerCastsSpell()} / {@code youCastSpell()} with an instant-or-sorcery filter and "targets only
 * this creature" as its stack-entry filter). As it resolves it reads the stack entry's triggering spell (the spell
 * while it is still on the stack, else as it last existed there, CR 608.2h): the spell's controller controls the
 * copies, and "other" is other than the ability's source.
 * <p>
 * With {@code chooseOne} (Beamsplitter Mage) the caster instead chooses one of those creatures and makes one copy
 * targeting it; its intervening "if" ("if you control one or more other creatures that spell could target") is the
 * trigger's {@code StackEntryCouldTargetAnotherPermanentPredicate} filter, and no copy is made when no such creature is
 * left as the ability resolves.
 *
 * @param chooseOne whether one chosen creature gets one copy, rather than each creature a copy
 */
public record CopySpellForEachOtherControlledCreatureEffect(boolean chooseOne) implements CardEffect {

    public CopySpellForEachOtherControlledCreatureEffect() {
        this(false);
    }
}
