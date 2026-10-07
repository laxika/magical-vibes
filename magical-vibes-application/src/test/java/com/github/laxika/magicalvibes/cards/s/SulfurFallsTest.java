package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SulfurFalls.class, Island.class, Mountain.class, Swamp.class})
class SulfurFallsTest extends BaseCardTest {

    // ===== Enters tapped (no qualifying lands) =====

    @Test
    @DisplayName("Enters tapped when you control no lands")
    void entersTappedWithNoLands() {
        harness.setHand(player1, List.of(new SulfurFalls()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent falls = findFalls(player1);
        assertThat(falls.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you only control non-matching lands (Swamp)")
    void entersTappedWithNonMatchingLands() {
        harness.addToBattlefield(player1, new Swamp());

        harness.setHand(player1, List.of(new SulfurFalls()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent falls = findFalls(player1);
        assertThat(falls.isTapped()).isTrue();
    }

    // ===== Enters untapped (qualifying lands present) =====

    @Test
    @DisplayName("Enters untapped when you control an Island")
    void entersUntappedWithIsland() {
        harness.addToBattlefield(player1, new Island());

        harness.setHand(player1, List.of(new SulfurFalls()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent falls = findFalls(player1);
        assertThat(falls.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control a Mountain")
    void entersUntappedWithMountain() {
        harness.addToBattlefield(player1, new Mountain());

        harness.setHand(player1, List.of(new SulfurFalls()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent falls = findFalls(player1);
        assertThat(falls.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control both an Island and a Mountain")
    void entersUntappedWithBoth() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());

        harness.setHand(player1, List.of(new SulfurFalls()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent falls = findFalls(player1);
        assertThat(falls.isTapped()).isFalse();
    }

    // ===== Only checks your lands, not opponent's =====

    @Test
    @DisplayName("Opponent's Island does not satisfy the check")
    void opponentIslandDoesNotCount() {
        harness.addToBattlefield(player2, new Island());

        harness.setHand(player1, List.of(new SulfurFalls()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent falls = findFalls(player1);
        assertThat(falls.isTapped()).isTrue();
    }

    // ===== Mana production =====

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        addCreatureReady(player1, new SulfurFalls());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        addCreatureReady(player1, new SulfurFalls());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    // ===== Helpers =====

    @Test
    @DisplayName("A tapped Island still lets Sulfur Falls enter untapped")
    void entersUntappedWithTappedIsland() {
        harness.addToBattlefieldAndReturn(player1, new Island()).tap();
        harness.setHand(player1, List.of(new SulfurFalls()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findFalls(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Mountain still lets Sulfur Falls enter untapped")
    void entersUntappedWithTappedMountain() {
        harness.addToBattlefieldAndReturn(player1, new Mountain()).tap();
        harness.setHand(player1, List.of(new SulfurFalls()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findFalls(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Another Sulfur Falls does not satisfy the Island or Mountain check")
    void anotherSulfurFallsDoesNotCount() {
        harness.addToBattlefield(player1, new SulfurFalls());
        harness.setHand(player1, List.of(new SulfurFalls()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanents(player1, "Sulfur Falls")).hasSize(2);
        assertThat(findPermanents(player1, "Sulfur Falls").getLast().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's Mountain does not satisfy the check")
    void opponentMountainDoesNotCount() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new SulfurFalls()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findFalls(player1).isTapped()).isTrue();
    }

    private Permanent findFalls(Player player) {
        return findPermanent(player, "Sulfur Falls");
    }
}
