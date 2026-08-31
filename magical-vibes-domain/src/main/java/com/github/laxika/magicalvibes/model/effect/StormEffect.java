package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.UUID;

/**
 * Trigger descriptor for {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_SELF_CAST}
 * implementing the Storm keyword: "When you cast this spell, copy it for each spell cast before it
 * this turn. You may choose new targets for the copies."
 *
 * <p>At cast time {@code TriggerCollectionService.checkSpellCastTriggers} snapshots the just-cast
 * spell, counts the spells cast before it this turn, and queues a triggered ability wrapping a
 * {@link StormCopyEffect} that creates that many copies at resolution.</p>
 *
 * @param tokenCopy whether the copies enter the battlefield as tokens when they resolve
 * @param instantOrSorceryOnly whether to count only instant and sorcery spells cast by the
 *                             spell's controller, as used by Show of Confidence
 */
public record StormEffect(boolean tokenCopy, boolean instantOrSorceryOnly) implements SpellCastCopyTrigger {

    public StormEffect() {
        this(false, false);
    }

    public static StormEffect forInstantOrSorcery() {
        return new StormEffect(false, true);
    }

    @Override
    public boolean matches(Card spellCard) {
        return !instantOrSorceryOnly
                || spellCard.hasType(CardType.INSTANT)
                || spellCard.hasType(CardType.SORCERY);
    }

    @Override
    public CardEffect createCopyEffect(StackEntry spellSnapshot, UUID castingPlayerId, int copies) {
        return new StormCopyEffect(spellSnapshot, castingPlayerId, copies, tokenCopy);
    }
}
