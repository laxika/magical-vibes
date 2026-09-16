package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.Objects;
import java.util.UUID;

/** Static effect for Unbound Flourishing's cast-time doubling of X in permanent spells. */
public record DoubleXValueForPermanentSpellEffect() implements CastTimeXValueModifierEffect {

    @Override
    public boolean appliesTo(Card spell, UUID spellControllerId, UUID sourceControllerId) {
        return Objects.equals(spellControllerId, sourceControllerId)
                && spell.getParsedManaCost() != null
                && spell.getParsedManaCost().hasX()
                && java.util.Arrays.stream(CardType.values())
                        .anyMatch(type -> type.isPermanentType() && spell.hasType(type));
    }

    @Override
    public int modifyCastTimeXValue(int xValue) {
        return xValue * 2;
    }
}
