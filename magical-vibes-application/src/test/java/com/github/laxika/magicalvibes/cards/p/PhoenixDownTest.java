package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhoenixDown.class, AirElemental.class, GrizzlyBears.class, WalkingCorpse.class})
class PhoenixDownTest extends BaseCardTest {

    @Test
    void returnsAQualifyingCreatureTappedAndExilesPhoenixDown() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addToBattlefield(player1, new PhoenixDown());
        addActivationMana();

        harness.activateAbility(player1, 0, 0, 0, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Phoenix Down"));
    }

    @Test
    void exilesASelectedZombieAndPhoenixDown() {
        harness.addToBattlefield(player1, new PhoenixDown());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        addActivationMana();

        harness.activateAbility(player1, 0, 1, null, zombie.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phoenix Down");
        harness.assertNotOnBattlefield(player1, "Walking Corpse");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Phoenix Down", "Walking Corpse");
    }

    @Test
    void cannotExileAPermanentWithoutAnUndeadSubtype() {
        harness.addToBattlefield(player1, new PhoenixDown());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Phoenix Down");
    }

    @Test
    void cannotReturnACreatureWithManaValueGreaterThanFour() {
        AirElemental creature = new AirElemental();
        harness.setGraveyard(player1, List.of(creature));
        harness.addToBattlefield(player1, new PhoenixDown());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, 0, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Phoenix Down");
    }

    @Test
    void cannotReturnACreatureFromAnOpponentsGraveyard() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.addToBattlefield(player1, new PhoenixDown());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, 0, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Phoenix Down");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotReturnANoncreatureCard() {
        PhoenixDown artifact = new PhoenixDown();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addToBattlefield(player1, new PhoenixDown());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, 0, artifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Phoenix Down");
        harness.assertInGraveyard(player1, "Phoenix Down");
    }

    @Test
    void exilesAnOpponentsZombieAndPaysTheExileCostBeforeResolution() {
        harness.addToBattlefield(player1, new PhoenixDown());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        addActivationMana();

        harness.activateAbility(player1, 0, 1, null, zombie.getId());

        harness.assertNotOnBattlefield(player1, "Phoenix Down");
        harness.assertOnBattlefield(player2, "Walking Corpse");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).contains("Phoenix Down");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName()).contains("Walking Corpse");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PhoenixDown());
        source.setTapped(true);
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, zombie.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Phoenix Down");
        harness.assertOnBattlefield(player2, "Walking Corpse");
    }

    @Test
    void cannotActivateWithoutWhiteMana() {
        harness.addToBattlefield(player1, new PhoenixDown());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, zombie.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Phoenix Down");
        harness.assertOnBattlefield(player2, "Walking Corpse");
    }

    @Test
    void doesNotReturnATargetThatLeavesTheGraveyardBeforeResolution() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addToBattlefield(player1, new PhoenixDown());
        addActivationMana();

        harness.activateAbility(player1, 0, 0, 0, creature.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of());
        gd.getPlayerExiledCards(player1.getId()).add(creature);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Phoenix Down");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).contains("Phoenix Down", "Grizzly Bears");
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
