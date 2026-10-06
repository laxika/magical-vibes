package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WickerboughElder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Scarecrone.class, WickerboughElder.class})
class ScarecroneTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Scarecrow (itself) draws a card")
    void sacrificeScarecrowDrawsCard() {
        addReadyScarecrone(player1);
        harness.setLibrary(player1, List.of(new WickerboughElder()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        // Scarecrone is the only Scarecrow → auto-sacrifices itself.
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scarecrone");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Returns an artifact creature card from graveyard to battlefield")
    void returnsArtifactCreatureFromGraveyard() {
        addReadyScarecrone(player1);
        Scarecrone target = new Scarecrone();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(target.getId()));
        harness.assertNotInGraveyard(player1, "Scarecrone");
    }

    @Test
    @DisplayName("Cannot return a non-artifact creature card")
    void cannotReturnNonArtifactCreature() {
        addReadyScarecrone(player1);
        WickerboughElder invalidTarget = new WickerboughElder();
        harness.setGraveyard(player1, List.of(new Scarecrone(), invalidTarget));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null,
                invalidTarget.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Reanimation ability requires tapping and cannot be activated when tapped")
    void reanimationRequiresUntapped() {
        Permanent scarecrone = addReadyScarecrone(player1);
        scarecrone.tap();
        Scarecrone target = new Scarecrone();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    void sacrificeCostIsPaidBeforeDrawingAndDoesNotRequireUntappedOrSummoningReady() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Scarecrone());
        source.setSummoningSick(true);
        source.tap();
        harness.setLibrary(player1, List.of(new WickerboughElder()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Scarecrone");
        harness.assertInGraveyard(player1, "Scarecrone");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void canSacrificeAnotherScarecrow() {
        addReadyScarecrone(player1);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Scarecrone());
        harness.setLibrary(player1, List.of(new WickerboughElder()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(sacrifice.getCard().getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void cannotActivateReanimationWithoutTarget() {
        addReadyScarecrone(player1);
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOpponentsGraveyard() {
        addReadyScarecrone(player1);
        Scarecrone target = new Scarecrone();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null,
                target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reanimationDoesNotChooseReplacementWhenTargetLeavesGraveyard() {
        addReadyScarecrone(player1);
        Scarecrone target = new Scarecrone();
        Scarecrone other = new Scarecrone();
        harness.setGraveyard(player1, List.of(target, other));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of(other));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Scarecrone");
    }

    @Test
    void reanimationCannotBeActivatedWhileSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Scarecrone());
        source.setSummoningSick(true);
        Scarecrone target = new Scarecrone();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null,
                target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyScarecrone(Player player) {
        Permanent scarecrone = harness.addToBattlefieldAndReturn(player, new Scarecrone());
        scarecrone.setSummoningSick(false);
        return scarecrone;
    }
}
