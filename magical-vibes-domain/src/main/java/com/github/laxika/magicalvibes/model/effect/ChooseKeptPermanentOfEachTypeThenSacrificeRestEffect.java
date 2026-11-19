package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import java.util.List;

/**
 * Each player keeps one permanent for each listed type, then sacrifices the other permanents.
 *
 * <p>The spell's controller makes every choice, one player and one card type at a time (artifact →
 * creature → enchantment → planeswalker for Tragic Arrogance, or land for Cataclysm). A type the
 * player controls nothing of is skipped, and a permanent with several of those types may be chosen
 * for more than one of them, which is why earlier picks are not removed from later candidate lists.
 * All choices are made before anything is sacrificed, and the sacrifices then happen simultaneously.
 * {@code players} is {@code eachPlayer()} or {@code eachOpponent()}. Driven by
 * {@code ChooseKeptPermanentOfEachTypeThenSacrificeRestEffectHandler}.
 */
public record ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect(
        List<CardType> types, boolean sacrificeAllPermanents, boolean eachPlayerChooses,
        PlayerSubject players) implements CardEffect {

    private static final List<CardType> TRAGIC_ARROGANCE_TYPES = List.of(
            CardType.ARTIFACT, CardType.CREATURE, CardType.ENCHANTMENT, CardType.PLANESWALKER);

    public ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect() {
        this(TRAGIC_ARROGANCE_TYPES, false, false, Subjects.eachPlayer());
    }

    public ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect(
            List<CardType> types, boolean sacrificeAllPermanents, boolean eachPlayerChooses) {
        this(types, sacrificeAllPermanents, eachPlayerChooses, Subjects.eachPlayer());
    }

    public ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect {
        types = List.copyOf(types);
        if (!(players instanceof PlayerSubject.Each)) {
            throw new IllegalArgumentException("Each player or each opponent keeps and sacrifices: " + players);
        }
    }
}
