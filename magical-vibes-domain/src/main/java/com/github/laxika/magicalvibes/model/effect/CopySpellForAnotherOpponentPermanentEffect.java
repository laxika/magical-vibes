package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose one of those permanents. Copy that spell. The copy targets the chosen permanent" (Exterminator Magmarch): the
 * effect of a filtered cast trigger ("an instant or sorcery spell that targets only a single nonland permanent an
 * opponent controls"), whose intervening "if" ("if another opponent controls one or more nonland permanents that spell
 * could target") is its {@code StackEntryCouldTargetAnotherPermanentPredicate} filter. As it resolves it reads the stack
 * entry's triggering spell (the spell while it is still on the stack, else as it last existed there, CR 608.2h) and the
 * controller of the permanent it targeted (as it last existed, if it left the battlefield); no copy is made when no
 * such permanent is left.
 */
public record CopySpellForAnotherOpponentPermanentEffect() implements CardEffect {
}
