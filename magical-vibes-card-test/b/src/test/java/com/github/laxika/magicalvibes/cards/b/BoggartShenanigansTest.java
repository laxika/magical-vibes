package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SkirkProspector;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoggartShenanigans.class, GrizzlyBears.class, LilianaVess.class, Naturalize.class,
        Shock.class, SkirkProspector.class})
class BoggartShenanigansTest extends BaseCardTest {

    // "Whenever another Goblin you control is put into a graveyard from the battlefield,
    //  you may have this enchantment deal 1 damage to target player or planeswalker."

    /** Player1 shocks their own creature and resolves Shock, leaving the death trigger's target choice active. */
    private void killWithShock(String targetName) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(player1, targetName);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    @Test
    @DisplayName("Accepting deals 1 damage to the chosen opponent when a Goblin dies")
    void acceptingDealsDamageToOpponent() {
        harness.addToBattlefield(player1, new BoggartShenanigans());
        harness.addToBattlefield(player1, new SkirkProspector()); // 1/1 Goblin

        int p2LifeBefore = gd.getLife(player2.getId());

        killWithShock("Skirk Prospector");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 1);
    }

    @Test
    @DisplayName("Any player is a legal target — controller may be chosen")
    void canTargetController() {
        harness.addToBattlefield(player1, new BoggartShenanigans());
        harness.addToBattlefield(player1, new SkirkProspector());

        int p1LifeBefore = gd.getLife(player1.getId());

        killWithShock("Skirk Prospector");

        // The controller is a valid target (not opponent-restricted).
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(p1LifeBefore - 1);
    }

    @Test
    @DisplayName("An opposing planeswalker is offered as a target and loses a loyalty counter")
    void canTargetPlaneswalker() {
        harness.addToBattlefield(player1, new BoggartShenanigans());
        harness.addToBattlefield(player1, new SkirkProspector());
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        liliana.setCounterCount(CounterType.LOYALTY, 5);

        killWithShock("Skirk Prospector");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(liliana.getId());

        harness.handlePermanentChosen(player1, liliana.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("A creature is not a legal target for a player-or-planeswalker ability")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new BoggartShenanigans());
        harness.addToBattlefield(player1, new SkirkProspector());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithShock("Skirk Prospector");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(bears.getId());
    }

    @Test
    @DisplayName("Declining the may ability deals no damage")
    void decliningDealsNoDamage() {
        harness.addToBattlefield(player1, new BoggartShenanigans());
        harness.addToBattlefield(player1, new SkirkProspector());

        int p2LifeBefore = gd.getLife(player2.getId());

        killWithShock("Skirk Prospector");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore);
    }

    @Test
    @DisplayName("A non-Goblin creature dying does not trigger the enchantment")
    void nonGoblinDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new BoggartShenanigans());
        harness.addToBattlefield(player1, new GrizzlyBears()); // Bear, not Goblin

        int p2LifeBefore = gd.getLife(player2.getId());

        killWithShock("Grizzly Bears");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore);
    }

    @Test
    @DisplayName("Another noncreature Goblin going to the graveyard triggers the enchantment")
    void anotherGoblinEnchantmentGoingToGraveyardTriggers() {
        harness.addToBattlefield(player1, new BoggartShenanigans());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0, other.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("The enchantment does not trigger for its own trip to the graveyard")
    void ownDepartureDoesNotTrigger() {
        Permanent shenanigans = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, shenanigans.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Goblin dying does not trigger the enchantment")
    void opposingGoblinDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new BoggartShenanigans());
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new SkirkProspector());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, goblin.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
