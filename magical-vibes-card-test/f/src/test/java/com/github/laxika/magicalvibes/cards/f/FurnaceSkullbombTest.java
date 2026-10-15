package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FurnaceSkullbomb.class, CopperLonglegs.class, PropheticPrism.class})
class FurnaceSkullbombTest extends BaseCardTest {

    @Test
    @DisplayName("The basic ability sacrifices Furnace Skullbomb and draws a card")
    void sacrificesAndDraws() {
        Permanent skullbomb = harness.addToBattlefieldAndReturn(player1, new FurnaceSkullbomb());
        CopperLonglegs draw = new CopperLonglegs();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(skullbomb);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(skullbomb.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    @DisplayName("The second ability sacrifices, adds oil counters, and draws")
    void sacrificesAddsOilCountersAndDraws() {
        Permanent skullbomb = harness.addToBattlefieldAndReturn(player1, new FurnaceSkullbomb());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        CopperLonglegs draw = new CopperLonglegs();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(skullbomb);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(skullbomb.getCard());
        assertThat(artifact.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    @DisplayName("The second ability can target only an artifact or creature you control")
    void targetMustBeOwnArtifactOrCreature() {
        harness.addToBattlefieldAndReturn(player1, new FurnaceSkullbomb());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Furnace Skullbomb");
    }

    @Test
    @DisplayName("The second ability can be activated only at sorcery speed")
    void sorcerySpeedOnly() {
        harness.addToBattlefieldAndReturn(player1, new FurnaceSkullbomb());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The second ability adds oil counters to a nonartifact creature")
    void addsOilToCreature() {
        harness.addToBattlefield(player1, new FurnaceSkullbomb());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        CopperLonglegs draw = new CopperLonglegs();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.assertNotOnBattlefield(player1, "Furnace Skullbomb");
        harness.assertInGraveyard(player1, "Furnace Skullbomb");
        assertThat(creature.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(draw);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    @DisplayName("The basic draw ability works during an opponent's turn")
    void basicAbilityWorksAtInstantSpeed() {
        harness.addToBattlefield(player1, new FurnaceSkullbomb());
        CopperLonglegs draw = new CopperLonglegs();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, 0, null);
        harness.assertInGraveyard(player1, "Furnace Skullbomb");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(draw);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    @DisplayName("Targeting the sacrificed Skullbomb prevents the second ability's draw")
    void targetingSelfDoesNotDraw() {
        Permanent skullbomb = harness.addToBattlefieldAndReturn(player1, new FurnaceSkullbomb());
        CopperLonglegs draw = new CopperLonglegs();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, skullbomb.getId());
        harness.assertInGraveyard(player1, "Furnace Skullbomb");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(draw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing the targeted artifact in response prevents the second ability's draw")
    void targetLeavingBattlefieldPreventsDraw() {
        harness.addToBattlefield(player1, new FurnaceSkullbomb());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FurnaceSkullbomb());
        CopperLonglegs firstDraw = new CopperLonglegs();
        CopperLonglegs secondDraw = new CopperLonglegs();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.activateAbility(player1, 0, 0, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstDraw).doesNotContain(secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondDraw);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The second ability cannot be activated with another ability on the stack")
    void secondAbilityRequiresEmptyStack() {
        harness.addToBattlefield(player1, new FurnaceSkullbomb());
        harness.addToBattlefield(player1, new FurnaceSkullbomb());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.setLibrary(player1, List.of(new CopperLonglegs()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Furnace Skullbomb");
        assertThat(target.getCounterCount(CounterType.OIL)).isZero();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The second ability cannot be activated during your upkeep")
    void secondAbilityRequiresMainPhase() {
        harness.addToBattlefield(player1, new FurnaceSkullbomb());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Furnace Skullbomb");
        assertThat(target.getCounterCount(CounterType.OIL)).isZero();
    }
}
