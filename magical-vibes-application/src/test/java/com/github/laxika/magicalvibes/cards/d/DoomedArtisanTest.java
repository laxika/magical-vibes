package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DoomedArtisan.class, GrizzlyBears.class})
class DoomedArtisanTest extends BaseCardTest {

    @Test
    @DisplayName("Your end step creates a colorless Sculpture artifact creature token")
    void endStepCreatesSculptureToken() {
        addReadyArtisan(player1);

        createSculptureAtEndStep();

        Permanent sculpture = findPermanent(player1, "Sculpture");
        assertThat(sculpture.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sculpture)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sculpture)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sculpture power and toughness update with the number of Sculptures controlled")
    void sculptureStatsUpdateDynamically() {
        addReadyArtisan(player1);

        createSculptureAtEndStep();
        Permanent first = findPermanent(player1, "Sculpture");
        createSculptureAtEndStep();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sculptures you control can't attack or block")
    void sculpturesCannotAttackOrBlock() {
        addReadyArtisan(player1);
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

        Permanent attacker = addReadyCreature(player2);
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

    private void createSculptureAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addReadyArtisan(Player player) {
        Permanent artisan = new Permanent(new DoomedArtisan());
        artisan.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(artisan);
        return artisan;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
