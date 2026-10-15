package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HinterlandHarbor.class, Forest.class, Island.class, Mountain.class})
class HinterlandHarborTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no lands")
    void entersTappedWithNoLands() {
        harness.setHand(player1, List.of(new HinterlandHarbor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent harbor = findHarbor(player1);
        assertThat(harbor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you only control non-matching lands (Mountain)")
    void entersTappedWithNonMatchingLands() {
        harness.addToBattlefield(player1, new Mountain());

        harness.setHand(player1, List.of(new HinterlandHarbor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent harbor = findHarbor(player1);
        assertThat(harbor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Forest")
    void entersUntappedWithForest() {
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new HinterlandHarbor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent harbor = findHarbor(player1);
        assertThat(harbor.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control an Island")
    void entersUntappedWithIsland() {
        harness.addToBattlefield(player1, new Island());

        harness.setHand(player1, List.of(new HinterlandHarbor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent harbor = findHarbor(player1);
        assertThat(harbor.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control both a Forest and an Island")
    void entersUntappedWithBoth() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        harness.setHand(player1, List.of(new HinterlandHarbor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent harbor = findHarbor(player1);
        assertThat(harbor.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's Forest does not satisfy the check")
    void opponentForestDoesNotCount() {
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new HinterlandHarbor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent harbor = findHarbor(player1);
        assertThat(harbor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        addCreatureReady(player1, new HinterlandHarbor());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        addCreatureReady(player1, new HinterlandHarbor());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped Forest still lets Hinterland Harbor enter untapped")
    void tappedForestCounts() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        harness.setHand(player1, List.of(new HinterlandHarbor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findHarbor(player1).isTapped()).isFalse();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Another Hinterland Harbor is neither a Forest nor an Island")
    void anotherHarborDoesNotCount() {
        harness.addToBattlefield(player1, new HinterlandHarbor());
        harness.setHand(player1, List.of(new HinterlandHarbor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getLast().isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Island does not satisfy the entry condition")
    void opponentIslandDoesNotCount() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new HinterlandHarbor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findHarbor(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("An untapped Harbor can produce mana immediately after being played")
    void producesManaOnTurnItEnters() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new HinterlandHarbor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(findHarbor(player1).isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Harbor that entered tapped cannot activate either mana ability")
    void cannotProduceManaWhileTapped() {
        harness.setHand(player1, List.of(new HinterlandHarbor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    private Permanent findHarbor(Player player) {
        return findPermanent(player, "Hinterland Harbor");
    }
}
