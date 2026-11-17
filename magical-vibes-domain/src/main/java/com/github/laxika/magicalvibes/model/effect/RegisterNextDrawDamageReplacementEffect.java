package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Words of War's activated ability: registers a one-shot replacement effect (CR 614.1a) of the
 * controller's next draw this turn with 2 damage to the ability's chosen target. The resolution makes a
 * floating effect on the controller whose payload is this record with the damage source's last-known card and
 * the chosen target; the source permanent is the floating effect's own.
 *
 * @param sourceCard the last-known card that deals the replacement damage; {@code null} on the card's own copy
 * @param targetId   the target chosen when the ability was activated; {@code null} on the card's own copy
 */
public record RegisterNextDrawDamageReplacementEffect(Card sourceCard, UUID targetId) implements CardEffect {

    public RegisterNextDrawDamageReplacementEffect() {
        this(null, null);
    }


    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.anyTarget());
    }
}
