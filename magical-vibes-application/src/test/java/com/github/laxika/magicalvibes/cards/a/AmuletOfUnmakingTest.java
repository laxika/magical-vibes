package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.l.LionsEyeDiamond;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PrismaticCircle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AmuletOfUnmaking.class, FemerefScouts.class, Plains.class, LionsEyeDiamond.class,
        PrismaticCircle.class})
class AmuletOfUnmakingTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability exiles the target creature")
    void exilesTargetCreature() {
        addCreatureReady(player1, new AmuletOfUnmaking());
        Permanent target = addCreatureReady(player2, new FemerefScouts());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Femeref Scouts");
        harness.assertNotInGraveyard(player2, "Femeref Scouts");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Femeref Scouts"));
    }

    @Test
    @DisplayName("Can exile a land")
    void exilesTargetLand() {
        addCreatureReady(player1, new AmuletOfUnmaking());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Can exile an artifact")
    void exilesTargetArtifact() {
        addCreatureReady(player1, new AmuletOfUnmaking());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LionsEyeDiamond());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Lion's Eye Diamond"));
    }

    @Test
    @DisplayName("Cannot target a permanent that is neither an artifact, creature, nor land")
    void cannotTargetIneligiblePermanent() {
        addCreatureReady(player1, new AmuletOfUnmaking());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrismaticCircle());
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    @Test
    @DisplayName("Amulet is exiled as a cost, not sacrificed")
    void amuletExiledAsCost() {
        addCreatureReady(player1, new AmuletOfUnmaking());
        Permanent target = addCreatureReady(player2, new FemerefScouts());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Amulet of Unmaking");
        harness.assertNotInGraveyard(player1, "Amulet of Unmaking");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Amulet of Unmaking"));
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new AmuletOfUnmaking());
        Permanent target = addCreatureReady(player2, new FemerefScouts());
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate during the opponent's turn (sorcery speed only)")
    void cannotActivateAtInstantSpeed() {
        addCreatureReady(player1, new AmuletOfUnmaking());
        Permanent target = addCreatureReady(player2, new FemerefScouts());
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent amulet = addCreatureReady(player1, new AmuletOfUnmaking());
        amulet.setTapped(true);
        Permanent target = addCreatureReady(player2, new FemerefScouts());
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        harness.assertOnBattlefield(player1, "Amulet of Unmaking");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate during combat on its controller's turn")
    void cannotActivateDuringCombat() {
        addCreatureReady(player1, new AmuletOfUnmaking());
        Permanent target = addCreatureReady(player2, new FemerefScouts());
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertOnBattlefield(player1, "Amulet of Unmaking");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while another ability is on the stack")
    void cannotActivateWithNonemptyStack() {
        addCreatureReady(player1, new AmuletOfUnmaking());
        Permanent secondAmulet = addCreatureReady(player1, new AmuletOfUnmaking());
        Permanent target = addCreatureReady(player2, new FemerefScouts());
        harness.addMana(player1, ManaColor.WHITE, 10);
        harness.activateAbility(player1, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondAmulet);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Femeref Scouts");
    }

    @Test
    @DisplayName("A newly entered noncreature Amulet can activate immediately")
    void newlyEnteredAmuletCanActivate() {
        harness.addToBattlefield(player1, new AmuletOfUnmaking());
        Permanent target = addCreatureReady(player2, new FemerefScouts());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Amulet of Unmaking"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Femeref Scouts"));
    }

    @Test
    @DisplayName("Can target itself, leaving the ability without a legal target after paying costs")
    void canTargetItself() {
        Permanent amulet = addCreatureReady(player1, new AmuletOfUnmaking());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, amulet.getId());

        harness.assertNotOnBattlefield(player1, "Amulet of Unmaking");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(c -> c.getName().equals("Amulet of Unmaking"))
                .hasSize(1);
    }
}
