package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RadiantLotus.class, LotusPetal.class, GrizzlyBears.class})
class RadiantLotusTest extends BaseCardTest {

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The controller chooses any color during an opponent's turn")
    void canActivateOnOpponentsTurnAndChooseAnyColor(ManaColor color) {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RadiantLotus());
        harness.addToBattlefield(player1, new RadiantLotus());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.activateAbility(player1, 0, 0, 1, player2.getId());
        harness.handlePermanentChosen(player1, source.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();

        harness.passBothPriorities();
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player2.getId()).get(color)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.assertInGraveyard(player1, "Radiant Lotus");
    }

    @Test
    @DisplayName("Sacrificing the source first preserves the ability and sacrifice count")
    void canSacrificeSourceBeforeOtherArtifacts() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RadiantLotus());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new RadiantLotus());
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new RadiantLotus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, 2, player1.getId());
        harness.handlePermanentChosen(player1, source.getId());
        harness.handlePermanentChosen(player1, other.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);

        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(6);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("An opponent's artifact cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsArtifact() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RadiantLotus());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new RadiantLotus());
        Permanent opponentsArtifact = harness.addToBattlefieldAndReturn(player2, new RadiantLotus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, 1, player2.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentsArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentsArtifact);

        harness.handlePermanentChosen(player1, ownArtifact.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
    }

    @Test
    @DisplayName("A tapped Radiant Lotus cannot activate its tap ability")
    void cannotActivateWhileTapped() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RadiantLotus());
        harness.addToBattlefield(player1, new RadiantLotus());
        source.setTapped(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Sacrificing artifacts adds three mana per artifact to the target player")
    void sacrificesArtifactsAndAddsManaToTargetPlayer() {
        Permanent lotus = harness.addToBattlefieldAndReturn(player1, new RadiantLotus());
        Permanent petal1 = harness.addToBattlefieldAndReturn(player1, new LotusPetal());
        Permanent petal2 = harness.addToBattlefieldAndReturn(player1, new LotusPetal());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, 2, player2.getId());
        harness.handlePermanentChosen(player1, petal1.getId());
        harness.handlePermanentChosen(player1, petal2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE))
                .isEqualTo(6);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE))
                .isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lotus);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Lotus Petal")))
                .hasSize(2);
        assertThat(lotus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability requires at least one artifact to be sacrificed")
    void requiresAtLeastOneArtifact() {
        harness.addToBattlefieldAndReturn(player1, new RadiantLotus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a permanent")
    void rejectsNonPlayerTarget() {
        harness.addToBattlefieldAndReturn(player1, new RadiantLotus());
        harness.addToBattlefieldAndReturn(player1, new LotusPetal());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
