package com.github.laxika.magicalvibes.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The floating effects a resolution makes for outside-the-game play permissions and for "exile it instead"
 * replacements, through {@link GameData}'s queries.
 */
class GameDataResolutionReplacementTest {

    private GameData gd;
    private UUID playerId;
    private final UUID first = UUID.randomUUID();
    private final UUID second = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", playerId, "Player1");
    }

    @Test
    @DisplayName("Playing a card spends one single-play permission; another one naming it still permits the rest")
    void playingACardSpendsOneSinglePlayPermission() {
        gd.permitPlayFromOutsideGame("Wish", null, playerId, List.of(first, second), true, false);
        gd.permitPlayFromOutsideGame("Wish", null, playerId, List.of(first, second), true, false);

        gd.spendPlayFromOutsideGame(first);

        assertThat(gd.mayPlayFromOutsideGame(first)).isFalse();
        assertThat(gd.mayPlayFromOutsideGame(second)).isTrue();

        gd.spendPlayFromOutsideGame(second);

        assertThat(gd.mayPlayFromOutsideGame(second)).isFalse();
    }

    @Test
    @DisplayName("A permission for several cards one by one keeps permitting the others")
    void aPermissionForSeveralCardsKeepsTheOthers() {
        gd.permitPlayFromOutsideGame("Frontier Explorer", null, playerId, List.of(first, second), false, true);

        gd.spendPlayFromOutsideGame(first);

        assertThat(gd.mayPlayFromOutsideGame(first)).isFalse();
        assertThat(gd.mayPlayFromOutsideGame(second)).isTrue();
        assertThat(gd.mayChooseAdditionalModeFromOutsideGame(second)).isTrue();
    }

    @Test
    @DisplayName("An \"exile it instead\" rider is made once per card and used up once")
    void exileInsteadRiderIsUsedUpOnce() {
        gd.exileInsteadOfGraveyard("Nita, Forum Conciliator", playerId, first);
        gd.exileInsteadOfGraveyard("Nita, Forum Conciliator", playerId, first);

        assertThat(gd.exilesInsteadOfGraveyard(first)).isTrue();
        assertThat(gd.exilesInsteadOfGraveyard(second)).isFalse();
        assertThat(gd.endExileInsteadOfGraveyard(first)).isTrue();
        assertThat(gd.exilesInsteadOfGraveyard(first)).isFalse();
        assertThat(gd.endExileInsteadOfGraveyard(first)).isFalse();
    }
}
