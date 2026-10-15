package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeapotSlinger.class, GrizzlyBears.class, BarkformHarvester.class})
class TeapotSlingerTest extends BaseCardTest {

    @Test
    @DisplayName("Crossing four mana triggers once, before the spell resolves")
    void crossingThresholdTriggersOnlyOncePerTurn() {
        harness.addToBattlefield(player1, new TeapotSlinger());
        harness.setHand(player1, List.of(new BarkformHarvester(), new BarkformHarvester(),
                new BarkformHarvester()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting Teapot Slinger does not trigger its own expend ability")
    void doesNotTriggerForItsOwnCastingOrLaterSpending() {
        harness.setHand(player1, List.of(new TeapotSlinger(), new BarkformHarvester()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A single four-mana spell triggers each Teapot Slinger independently")
    void singleSpellTriggersEachExistingSlinger() {
        harness.addToBattlefield(player1, new TeapotSlinger());
        harness.addToBattlefield(player1, new TeapotSlinger());
        harness.setHand(player1, List.of(new TeapotSlinger()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent expending four does not trigger Teapot Slinger")
    void opponentsSpendingDoesNotTrigger() {
        harness.addToBattlefield(player2, new TeapotSlinger());
        harness.setHand(player1, List.of(new TeapotSlinger()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana spent activating abilities does not count toward expend")
    void activationManaDoesNotCount() {
        harness.addToBattlefield(player1, new TeapotSlinger());
        harness.addToBattlefield(player1, new BarkformHarvester());
        BarkformHarvester graveyardCard = new BarkformHarvester();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new BarkformHarvester()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 1, null, graveyardCard.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Menace prevents blocking Teapot Slinger with a single creature")
    void menaceRequiresTwoBlockers() {
        addCreatureReady(player1, new TeapotSlinger());
        addCreatureReady(player2, new BarkformHarvester());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace allows two creatures to block Teapot Slinger")
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new TeapotSlinger());
        Permanent firstBlocker = addCreatureReady(player2, new BarkformHarvester());
        Permanent secondBlocker = addCreatureReady(player2, new BarkformHarvester());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Expend can trigger again on a later turn")
    void expendResetsEachTurn() {
        harness.addToBattlefield(player1, new TeapotSlinger());
        harness.setHand(player1, List.of(new BarkformHarvester(), new BarkformHarvester()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);

        harness.setLibrary(player1, List.of(new BarkformHarvester(), new BarkformHarvester()));
        harness.setLibrary(player2, List.of(new BarkformHarvester(), new BarkformHarvester()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BarkformHarvester(), new BarkformHarvester()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger before four total mana is spent on spells")
    void doesNotTriggerBelowExpendThreshold() {
        harness.addToBattlefield(player1, new TeapotSlinger());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Deals 2 damage to each opponent when its controller expends four")
    void dealsDamageWhenControllerExpendsFour() {
        harness.addToBattlefield(player1, new TeapotSlinger());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        int opponentLifeBefore = gd.getLife(player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }
}
