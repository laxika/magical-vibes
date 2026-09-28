package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/** Each opponent chooses money, friends, or secrets, then the matching rewards are applied. */
public record EachOpponentChoosesMasterOfCeremoniesEffect(
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId,
        List<UUID> moneyPlayerIds,
        List<UUID> friendsPlayerIds,
        List<UUID> secretsPlayerIds
) implements CardEffect {

    public EachOpponentChoosesMasterOfCeremoniesEffect() {
        this(null, null, List.of(), List.of(), List.of());
    }

    public EachOpponentChoosesMasterOfCeremoniesEffect {
        if (remainingOpponentIds != null) {
            remainingOpponentIds = List.copyOf(remainingOpponentIds);
        }
        moneyPlayerIds = moneyPlayerIds == null ? List.of() : List.copyOf(moneyPlayerIds);
        friendsPlayerIds = friendsPlayerIds == null ? List.of() : List.copyOf(friendsPlayerIds);
        secretsPlayerIds = secretsPlayerIds == null ? List.of() : List.copyOf(secretsPlayerIds);
    }
}
