package com.github.laxika.magicalvibes.model.effect;

/**
 * "Each other player copies that spell. Each of those players may choose new targets for their copy" (Hive Mind), or,
 * with {@code optional}, "each other player may copy that spell" (Curse of Echoes): the effect of a filtered cast
 * trigger, whose filters say which spells (an instant or sorcery, cast by the enchanted player). As it resolves it reads
 * the stack entry's triggering spell (the spell while it is still on the stack, so targets changed in the meantime
 * carry over, else as it last existed there, CR 608.2h); "other" is other than the spell's controller.
 *
 * @param optional whether each other player may decline the copy
 */
public record CopySpellForEachOtherPlayerEffect(boolean optional) implements CardEffect {

    public CopySpellForEachOtherPlayerEffect() {
        this(false);
    }
}
