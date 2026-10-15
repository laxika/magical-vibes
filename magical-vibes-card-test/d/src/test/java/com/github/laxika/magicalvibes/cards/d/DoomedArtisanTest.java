package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DoomedArtisan.class})
class DoomedArtisanTest extends BaseCardTest {

    @Test
    @DisplayName("Your end step creates a colorless Sculpture artifact creature token")
    void endStepCreatesSculptureToken() {
        addCreatureReady(player1, new DoomedArtisan());

        createSculptureAtEndStep();

        Permanent sculpture = findPermanent(player1, "Sculpture");
        assertThat(sculpture.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sculpture)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sculpture)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sculpture power and toughness update with the number of Sculptures controlled")
    void sculptureStatsUpdateDynamically() {
        addCreatureReady(player1, new DoomedArtisan());

        createSculptureAtEndStep();
        Permanent first = findPermanent(player1, "Sculpture");
        createSculptureAtEndStep();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sculptures you control can't attack or block")
    void sculpturesCannotAttackOrBlock() {
        addCreatureReady(player1, new DoomedArtisan());
        createSculptureAtEndStep();
        Permanent sculpture = findPermanent(player1, "Sculpture");
        sculpture.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int sculptureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sculpture);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(sculptureIndex)))
                .isInstanceOf(IllegalStateException.class);

        Permanent attacker = addCreatureReady(player2, new DoomedArtisan());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(sculptureIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The created token is a colorless Sculpture artifact creature")
    void createdTokenHasItsSpecifiedCharacteristics() {
        addCreatureReady(player1, new DoomedArtisan());
        createSculptureAtEndStep();

        Permanent sculpture = findPermanent(player1, "Sculpture");
        assertThat(sculpture.getCard().isToken()).isTrue();
        assertThat(sculpture.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(sculpture.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(sculpture.getCard().getColors()).isEmpty();
        assertThat(sculpture.getCard().getSubtypes()).containsExactly(CardSubtype.SCULPTURE);
    }

    @Test
    @DisplayName("One remaining Artisan still prevents Sculptures from attacking and blocking")
    void restrictionRemainsUntilLastArtisanLeaves() {
        Permanent first = addCreatureReady(player1, new DoomedArtisan());
        addCreatureReady(player1, new DoomedArtisan());
        createSculptureAtEndStep();
        Permanent sculpture = findPermanent(player1, "Sculpture");
        sculpture.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(als.canAttack(gd, sculpture, player1.getId())).isFalse();
        assertThat(bls.canBlock(gd, sculpture)).isFalse();
    }

    @Test
    @DisplayName("An opponent's end step does not create a Sculpture for you")
    void opponentEndStepDoesNotTrigger() {
        addCreatureReady(player1, new DoomedArtisan());

        createSculptureAtEndStep(player2);

        assertThat(countPermanents(player1, "Sculpture")).isZero();
        assertThat(countPermanents(player2, "Sculpture")).isZero();
    }

    @Test
    @DisplayName("Each Artisan creates a token and all tokens count each other")
    void multipleArtisansCreateMultipleSculptures() {
        addCreatureReady(player1, new DoomedArtisan());
        addCreatureReady(player1, new DoomedArtisan());

        createSculptureAtEndStep();

        assertThat(findPermanents(player1, "Sculpture")).hasSize(2).allSatisfy(sculpture -> {
            assertThat(gqs.getEffectivePower(gd, sculpture)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, sculpture)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Sculptures count only those controlled by their own controller")
    void sculpturesCountOnlyTheirControllersSculptures() {
        addCreatureReady(player1, new DoomedArtisan());
        addCreatureReady(player2, new DoomedArtisan());
        createSculptureAtEndStep();
        createSculptureAtEndStep();
        createSculptureAtEndStep(player2);

        for (Permanent sculpture : findPermanents(player1, "Sculpture")) {
            assertThat(gqs.getEffectivePower(gd, sculpture)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, sculpture)).isEqualTo(2);
        }
        Permanent opponentsSculpture = findPermanent(player2, "Sculpture");
        assertThat(gqs.getEffectivePower(gd, opponentsSculpture)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentsSculpture)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sculptures shrink when another Sculpture leaves the battlefield")
    void sculptureStatsDecreaseWhenAnotherLeaves() {
        addCreatureReady(player1, new DoomedArtisan());
        createSculptureAtEndStep();
        Permanent first = findPermanent(player1, "Sculpture");
        createSculptureAtEndStep();
        Permanent second = findPermanents(player1, "Sculpture").get(1);

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sculptures can attack and block after the last Artisan leaves")
    void sculpturesCanFightAfterArtisanLeaves() {
        Permanent artisan = addCreatureReady(player1, new DoomedArtisan());
        createSculptureAtEndStep();
        Permanent sculpture = findPermanent(player1, "Sculpture");
        sculpture.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).remove(artisan);

        assertThat(als.canAttack(gd, sculpture, player1.getId())).isTrue();
        assertThat(bls.canBlock(gd, sculpture)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sculpture)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sculpture)).isEqualTo(1);
    }

    @Test
    @DisplayName("An Artisan does not restrict an opponent's Sculptures")
    void restrictionAppliesOnlyToControllersSculptures() {
        addCreatureReady(player1, new DoomedArtisan());
        Permanent opponentsArtisan = addCreatureReady(player2, new DoomedArtisan());
        createSculptureAtEndStep(player2);
        Permanent sculpture = findPermanent(player2, "Sculpture");
        sculpture.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).remove(opponentsArtisan);

        assertThat(als.canAttack(gd, sculpture, player2.getId())).isTrue();
        assertThat(bls.canBlock(gd, sculpture)).isTrue();
    }

    @Test
    @DisplayName("Removing the Artisan in response does not stop its end-step token")
    void endStepTriggerSurvivesSourceLeaving() {
        Permanent artisan = addCreatureReady(player1, new DoomedArtisan());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(artisan);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Sculpture")).isEqualTo(1);
        Permanent sculpture = findPermanent(player1, "Sculpture");
        assertThat(gqs.getEffectivePower(gd, sculpture)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sculpture)).isEqualTo(1);
    }

    private void createSculptureAtEndStep() {
        createSculptureAtEndStep(player1);
    }

    private void createSculptureAtEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
