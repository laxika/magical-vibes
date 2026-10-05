package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.p.PunctureBolt;
import com.github.laxika.magicalvibes.cards.w.Woeleecher;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IslebackSpawn.class, Island.class, PunctureBolt.class, Woeleecher.class})
class IslebackSpawnTest extends BaseCardTest {

    @Test
    @DisplayName("Shroud prevents Puncture Bolt from targeting Isleback Spawn")
    void shroudPreventsTargeting() {
        harness.addToBattlefield(player1, new IslebackSpawn());
        Permanent spawn = findSpawn();

        harness.setHand(player1, List.of(new PunctureBolt()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, spawn.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud prevents Woeleecher's ability from targeting Isleback Spawn")
    void shroudPreventsTargetingByAbility() {
        addCreatureReady(player1, new Woeleecher());
        Permanent spawn = addCreatureReady(player1, new IslebackSpawn());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, spawn.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Base 4/8 while both libraries have more than twenty cards")
    void noBoostWhenLibrariesLarge() {
        harness.setLibrary(player1, filler(21));
        harness.setLibrary(player2, filler(21));
        harness.addToBattlefield(player1, new IslebackSpawn());

        Permanent spawn = findSpawn();
        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(8);
    }

    @Test
    @DisplayName("Gets +4/+8 when a library has exactly twenty cards")
    void boostAtExactlyTwenty() {
        harness.setLibrary(player1, filler(20));
        harness.setLibrary(player2, filler(21));
        harness.addToBattlefield(player1, new IslebackSpawn());

        Permanent spawn = findSpawn();
        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(16);
    }

    @Test
    @DisplayName("Opponent's small library also grants the boost")
    void boostFromOpponentLibrary() {
        harness.setLibrary(player1, filler(21));
        harness.setLibrary(player2, filler(5));
        harness.addToBattlefield(player1, new IslebackSpawn());

        Permanent spawn = findSpawn();
        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(16);
    }

    @Test
    @DisplayName("Loses the boost once every library exceeds twenty cards again")
    void losesBoostWhenLibrariesGrow() {
        harness.setLibrary(player1, filler(10));
        harness.setLibrary(player2, filler(21));
        harness.addToBattlefield(player1, new IslebackSpawn());

        Permanent spawn = findSpawn();
        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(8);

        harness.setLibrary(player1, filler(21));
        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(8);
    }

    @Test
    @DisplayName("An empty library still grants the boost")
    void boostFromEmptyLibrary() {
        harness.setLibrary(player1, filler(21));
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new IslebackSpawn());

        Permanent spawn = findSpawn();
        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(16);
    }

    @Test
    @DisplayName("Two small libraries grant only one +4/+8 bonus")
    void boostDoesNotStackForMultipleLibraries() {
        harness.setLibrary(player1, filler(20));
        harness.setLibrary(player2, filler(20));
        harness.addToBattlefield(player1, new IslebackSpawn());

        Permanent spawn = findSpawn();
        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(16);
    }

    @Test
    @DisplayName("Gains the boost immediately when a library shrinks to twenty cards")
    void gainsBoostWhenLibraryShrinks() {
        harness.setLibrary(player1, filler(21));
        harness.setLibrary(player2, filler(21));
        harness.addToBattlefield(player1, new IslebackSpawn());

        Permanent spawn = findSpawn();
        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(8);

        harness.setLibrary(player2, filler(20));
        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(16);
    }

    @Test
    @DisplayName("Shroud also prevents an opponent's Puncture Bolt from targeting Isleback Spawn")
    void shroudPreventsOpponentTargeting() {
        harness.addToBattlefield(player1, new IslebackSpawn());
        Permanent spawn = findSpawn();
        harness.setHand(player2, List.of(new PunctureBolt()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, spawn.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    private List<Card> filler(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Island());
        }
        return cards;
    }

    private Permanent findSpawn() {
        return findPermanent(player1, "Isleback Spawn");
    }
}
