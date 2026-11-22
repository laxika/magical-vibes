package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.Trigger;

import java.util.List;
import java.util.Objects;

/**
 * "It perpetually gains "[ability]"", "they perpetually gain "...", "each [card] in your hand perpetually gains
 * "..."": the cards {@code cards} names gain {@code abilities} for as long as they are those cards (digital only).
 * A choice, random or the player's, is the {@code ChooseCardEffect} before it, over
 * {@code Subjects.cardsThisWay(StepKind.CHOOSE)}.
 *
 * @param cards     the cards that gain them; a hand's or library's are named in no log (CR 400.2)
 * @param abilities what each gains
 */
public record PerpetuallyGrantAbilitiesEffect(CardSubject cards, List<Ability> abilities) implements CardEffect {

    public PerpetuallyGrantAbilitiesEffect {
        Objects.requireNonNull(cards, "cards");
        abilities = List.copyOf(abilities);
        if (abilities.isEmpty()) {
            throw new IllegalArgumentException("Something is granted");
        }
    }

    /** One triggered ability with {@code trigger} and {@code effect}. */
    public PerpetuallyGrantAbilitiesEffect(CardSubject cards, Trigger trigger, CardEffect effect) {
        this(cards, List.of(new TriggeredAbility(trigger, effect)));
    }
}
