package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FalseFloor.class, FountainOfYouth.class, GrizzlyBears.class})
class FalseFloorTest extends BaseCardTest {

    @Test
    @DisplayName("False Floor and all creatures enter tapped")
    void permanentsEnterTapped() {
        harness.castFromHand(player1, new FalseFloor(), "{4}");
        harness.passBothPriorities();
        Permanent floor = findPermanent(player1, "False Floor");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        Permanent ownCreature = findPermanent(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        Permanent opposingCreature = findPermanent(player2, "Grizzly Bears");

        assertThat(floor.isTapped()).isTrue();
        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opposingCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability exiles only untapped creatures")
    void abilityExilesOnlyUntappedCreatures() {
        addReadyFalseFloor(player1);
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent untappedOwnCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent tappedOwnCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent untappedOpposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tappedOwnCreature.tap();
        untappedOwnCreature.untap();
        untappedOpposingCreature.untap();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "False Floor");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nonCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tappedOwnCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(untappedOwnCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(untappedOpposingCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("False Floor", "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("The ability can only be activated at sorcery speed")
    void abilityRequiresSorcerySpeed() {
        addReadyFalseFloor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exileCostIsPaidImmediatelyAndTappedStatusIsCheckedAtResolution() {
        addReadyFalseFloor(player1);
        Permanent initiallyUntapped = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent initiallyTapped = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        initiallyTapped.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "False Floor");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("False Floor");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(initiallyUntapped);
        initiallyUntapped.tap();
        initiallyTapped.untap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(initiallyUntapped);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(initiallyTapped);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName()).containsExactly("Grizzly Bears");
    }

    @Test
    void nonSpellCreaturesEnterTappedButNoncreatureArtifactsDoNot() {
        harness.enterBattlefieldAndReturn(player1, new FalseFloor());

        Permanent ownCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.enterBattlefieldAndReturn(player2, new FountainOfYouth());

        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opposingCreature.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    void creaturesEnterUntappedAfterFloorIsExiledAsCost() {
        addReadyFalseFloor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);

        Permanent creature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(creature.isTapped()).isFalse();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName()).containsExactly("Grizzly Bears");
    }

    @Test
    void tappedFloorCannotPayTapCost() {
        Permanent floor = addReadyFalseFloor(player1);
        floor.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "False Floor");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void abilityCannotBeActivatedDuringOpponentsMainPhase() {
        addReadyFalseFloor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "False Floor");
    }

    @Test
    void abilityCannotBeActivatedWithSpellOnStack() {
        addReadyFalseFloor(player1);
        harness.castFromHand(player1, new FountainOfYouth(), "{0}");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "False Floor");
        harness.passBothPriorities();
    }

    private Permanent addReadyFalseFloor(Player player) {
        Permanent floor = harness.addToBattlefieldAndReturn(player, new FalseFloor());
        floor.untap();
        return floor;
    }
}
