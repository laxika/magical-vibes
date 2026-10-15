package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ControlMagic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IslandSanctuary;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbueloAncestralEcho.class, GrizzlyBears.class, LiquimetalCoating.class, IslandSanctuary.class, ControlMagic.class})
class AbueloAncestralEchoTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers another creature and returns it at the next end step")
    void flickersOwnCreature() {
        harness.addToBattlefield(player1, new AbueloAncestralEcho());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(bearsId);
        assertThat(findPermanent(player1, "Grizzly Bears").isSummoningSick()).isTrue();
        assertThat(findPermanent(player1, "Abuelo, Ancestral Echo").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flickers another artifact")
    void flickersOwnArtifact() {
        harness.addToBattlefield(player1, new AbueloAncestralEcho());
        harness.addToBattlefield(player1, new LiquimetalCoating());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID coatingId = harness.getPermanentId(player1, "Liquimetal Coating");

        harness.activateAbility(player1, 0, null, coatingId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Liquimetal Coating");

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Liquimetal Coating");
        assertThat(harness.getPermanentId(player1, "Liquimetal Coating")).isNotEqualTo(coatingId);
    }

    @Test
    @DisplayName("Cannot target itself or an invalid permanent")
    void rejectsInvalidTargets() {
        harness.addToBattlefield(player1, new AbueloAncestralEcho());
        harness.addToBattlefield(player1, new IslandSanctuary());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID abueloId = harness.getPermanentId(player1, "Abuelo, Ancestral Echo");
        UUID sanctuaryId = harness.getPermanentId(player1, "Island Sanctuary");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, abueloId))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, sanctuaryId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void rejectsOpponentCreature() {
        harness.addToBattlefield(player1, new AbueloAncestralEcho());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability and delayed return survive Abuelo leaving the battlefield")
    void returnsAfterSourceLeavesBeforeResolution() {
        var source = harness.addToBattlefieldAndReturn(player1, new AbueloAncestralEcho());
        var target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Abuelo, Ancestral Echo");
    }

    @Test
    @DisplayName("An activation during the end step waits for the following turn's end step")
    void activationDuringEndStepReturnsNextTurn() {
        harness.addToBattlefield(player1, new AbueloAncestralEcho());
        var target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature controlled with Control Magic returns to its owner")
    void stolenCreatureReturnsToOwner() {
        harness.addToBattlefield(player1, new AbueloAncestralEcho());
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ControlMagic()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Control Magic");
        advanceToEndStep();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A target that leaves before resolution is not exiled or returned")
    void removedTargetDoesNotReturn() {
        harness.addToBattlefield(player1, new AbueloAncestralEcho());
        var target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();
        advanceToEndStep();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ward counters an opponent's activated ability when they cannot pay")
    void wardCountersOpponentAbility() {
        var source = harness.addToBattlefieldAndReturn(player1, new AbueloAncestralEcho());
        harness.addToBattlefield(player2, new LiquimetalCoating());

        harness.activateAbility(player2, 0, null, source.getId());
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isArtifact(gd, source)).isFalse();
        harness.assertOnBattlefield(player1, "Abuelo, Ancestral Echo");
    }

    @Test
    @DisplayName("Paying ward's two mana lets the opponent's ability resolve")
    void payingWardAllowsOpponentAbility() {
        var source = harness.addToBattlefieldAndReturn(player1, new AbueloAncestralEcho());
        harness.addToBattlefield(player2, new LiquimetalCoating());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, source.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isArtifact(gd, source)).isTrue();
    }

    @Test
    @DisplayName("The activation requires blue mana even when three mana are available")
    void cannotActivateWithoutBlueMana() {
        harness.addToBattlefield(player1, new AbueloAncestralEcho());
        var target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private void advanceToEndStep() {
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
