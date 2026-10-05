package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LaserScrewdriver.class, SolemnSimulacrum.class, Forest.class})
class LaserScrewdriverTest extends BaseCardTest {

    @Test
    void addsOneManaOfChosenColor() {
        harness.addToBattlefield(player1, new LaserScrewdriver());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void tapsTargetArtifact() {
        harness.addToBattlefield(player1, new LaserScrewdriver());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SolemnSimulacrum());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    void rejectsNonArtifactForTapAbility() {
        harness.addToBattlefield(player1, new LaserScrewdriver());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    void surveilsOne() {
        harness.addToBattlefield(player1, new LaserScrewdriver());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void goadsTargetCreatureUntilNextTurn() {
        harness.addToBattlefield(player1, new LaserScrewdriver());
        Permanent target = addCreatureReady(player2, new SolemnSimulacrum());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, null, target.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void surveilCanKeepTopCardWithoutChangingLibraryOrder() {
        harness.addToBattlefield(player1, new LaserScrewdriver());
        Forest top = new Forest();
        SolemnSimulacrum second = new SolemnSimulacrum();
        harness.setLibrary(player1, List.of(top, second));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void surveilWithEmptyLibraryDoesNotLoseTheGame() {
        harness.addToBattlefield(player1, new LaserScrewdriver());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.status).isNotEqualTo(com.github.laxika.magicalvibes.model.GameStatus.FINISHED);
    }

    @Test
    void canTapItselfAsTheTargetArtifact() {
        Permanent screwdriver = harness.addToBattlefieldAndReturn(player1, new LaserScrewdriver());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, screwdriver.getId());
        harness.passBothPriorities();

        assertThat(screwdriver.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rejectsNonCreatureForGoadAbility() {
        harness.addToBattlefield(player1, new LaserScrewdriver());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void tappedGoadedCreatureIsNotRequiredToAttack() {
        harness.addToBattlefield(player1, new LaserScrewdriver());
        Permanent target = addCreatureReady(player2, new SolemnSimulacrum());
        target.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, null, target.getId());
        harness.passBothPriorities();

        declareAttackers(player2, List.of());
        assertThat(target.isAttacking()).isFalse();
    }

    @Test
    void surveilMovesOnlyTheTopCardToGraveyard() {
        Permanent screwdriver = harness.addToBattlefieldAndReturn(player1, new LaserScrewdriver());
        Forest top = new Forest();
        SolemnSimulacrum second = new SolemnSimulacrum();
        harness.setLibrary(player1, List.of(top, second));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        assertThat(screwdriver.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }
}
