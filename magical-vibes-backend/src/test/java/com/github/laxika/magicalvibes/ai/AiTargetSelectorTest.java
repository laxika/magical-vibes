package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.e.EntrancingMelody;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AiTargetSelectorTest {

    private GameTestHarness harness;
    private Player human;
    private Player aiPlayer;
    private GameData gd;
    private AiTargetSelector targetSelector;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        human = harness.getPlayer1();
        aiPlayer = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();

        targetSelector = new AiTargetSelector(
                harness.getGameQueryService(), harness.getTargetValidationService());
    }

    // ===== findValidPermanentTargetsForManaValueX =====

    @Test
    @DisplayName("Returns creatures with mana value within affordable range")
    void returnsCreaturesWithAffordableManaValue() {
        // EliteVanguard MV=1, GrizzlyBears MV=2
        harness.addToBattlefield(human, new EliteVanguard());
        harness.addToBattlefield(human, new GrizzlyBears());

        EntrancingMelody card = new EntrancingMelody();
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 2);

        assertThat(targets).hasSize(2);
    }

    @Test
    @DisplayName("Excludes creatures with mana value exceeding maxX")
    void excludesCreaturesExceedingMaxX() {
        // EliteVanguard MV=1, GrizzlyBears MV=2
        harness.addToBattlefield(human, new EliteVanguard());
        harness.addToBattlefield(human, new GrizzlyBears());

        EntrancingMelody card = new EntrancingMelody();
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 1);

        assertThat(targets).hasSize(1);
        assertThat(targets.getFirst().getCard().getName()).isEqualTo("Elite Vanguard");
    }

    @Test
    @DisplayName("Returns empty list when no creatures have affordable mana value")
    void returnsEmptyWhenNoAffordableTargets() {
        // GrizzlyBears MV=2, but maxX=1
        harness.addToBattlefield(human, new GrizzlyBears());

        EntrancingMelody card = new EntrancingMelody();
        // maxX=0 means nothing is affordable (MV must be >= 1)
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 0);

        assertThat(targets).isEmpty();
    }

    @Test
    @DisplayName("Returns empty list when no creatures on any battlefield")
    void returnsEmptyWhenNoCreatures() {
        EntrancingMelody card = new EntrancingMelody();
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 5);

        assertThat(targets).isEmpty();
    }

    @Test
    @DisplayName("Includes creatures from both opponent and own battlefield")
    void includesCreaturesFromBothBattlefields() {
        harness.addToBattlefield(human, new EliteVanguard());
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());

        EntrancingMelody card = new EntrancingMelody();
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 5);

        assertThat(targets).hasSize(2);
    }

    @Test
    @DisplayName("Searches opponent battlefield before own")
    void searchesOpponentFirst() {
        harness.addToBattlefield(human, new EliteVanguard());
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());

        EntrancingMelody card = new EntrancingMelody();
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 5);

        // Opponent's creature should be listed before own
        assertThat(targets.getFirst().getCard().getName()).isEqualTo("Elite Vanguard");
    }

    @Test
    @DisplayName("Excludes non-creature permanents")
    void excludesNonCreatures() {
        // Island is a land, not a creature — should be excluded by the card's target filter
        harness.addToBattlefield(human, new Island());
        harness.addToBattlefield(human, new EliteVanguard());

        EntrancingMelody card = new EntrancingMelody();
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 5);

        assertThat(targets).hasSize(1);
        assertThat(targets.getFirst().getCard().getName()).isEqualTo("Elite Vanguard");
    }
}
