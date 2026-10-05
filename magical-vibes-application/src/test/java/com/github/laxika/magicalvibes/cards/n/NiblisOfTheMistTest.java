package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.cards.e.ExecutionersHood;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NiblisOfTheMist.class, YoungWolf.class, ExecutionersHood.class})
class NiblisOfTheMistTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ETB trigger prompts for may choice")
    void resolvingEtbPromptsForMayChoice() {
        harness.addToBattlefield(player2, new YoungWolf());

        castNiblis();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Young Wolf"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting ETB may taps target opponent creature")
    void acceptingMayTapsOpponentCreature() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new YoungWolf());

        castNiblisAndAcceptTarget(wolf.getId());

        assertThat(wolf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting ETB may can tap own creature")
    void acceptingMayCanTapOwnCreature() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new YoungWolf());

        castNiblisAndAcceptTarget(wolf.getId());

        assertThat(wolf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining ETB may leaves creature untapped")
    void decliningMayLeavesCreatureUntapped() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new YoungWolf());

        castNiblis();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wolf.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(wolf.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Niblis of the Mist");
    }

    @Test
    @DisplayName("ETB requires a creature target before the may choice and rejects noncreatures")
    void etbRequiresCreatureTargetAndRejectsNoncreature() {
        harness.addToBattlefield(player2, new ExecutionersHood());
        UUID hoodId = harness.getPermanentId(player2, "Executioner's Hood");

        castNiblis();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, hoodId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.assertOnBattlefield(player2, "Executioner's Hood");
    }

    @Test
    @DisplayName("ETB can target and tap Niblis itself when it is the only creature")
    void canTapItself() {
        castNiblis();
        harness.passBothPriorities();
        UUID niblisId = harness.getPermanentId(player1, "Niblis of the Mist");
        harness.handlePermanentChosen(player1, niblisId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped creature is a legal ETB target")
    void canTargetAlreadyTappedCreature() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new YoungWolf());
        wolf.setTapped(true);

        castNiblisAndAcceptTarget(wolf.getId());

        assertThat(wolf.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void castNiblisAndAcceptTarget(UUID targetId) {
        castNiblis();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    private void castNiblis() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new NiblisOfTheMist(), "{2}{W}");
    }
}
