package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JundSojourners.class, FlameJavelin.class, GrizzlyBears.class, LlanowarElves.class})
class JundSojournersTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, deals 1 damage to the chosen player")
    void diesDeals1DamageToPlayer() {
        harness.addToBattlefield(player1, new JundSojourners());
        int p2LifeBefore = gd.getLife(player2.getId());

        killWithFlameJavelin();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 1);
    }

    @Test
    @DisplayName("Death trigger any-target includes creatures — 1 damage kills a 1-toughness creature")
    void diesCanKillCreature() {
        harness.addToBattlefield(player1, new JundSojourners());
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        killWithFlameJavelin();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(elvesId, player2.getId());
        harness.handlePermanentChosen(player1, elvesId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cycling pings the chosen player and draws a card")
    void cyclingPingsPlayerAndDraws() {
        harness.setHand(player1, List.of(new JundSojourners()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        int p2LifeBefore = gd.getLife(player2.getId());

        harness.activateHandAbility(player1, 0, null);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 1);
        // The cycling draw still happens: Jund Sojourners discarded, the library card drawn.
        harness.assertInGraveyard(player1, "Jund Sojourners");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling can ping a creature and still draws a card")
    void cyclingPingsCreatureAndDraws() {
        harness.setHand(player1, List.of(new JundSojourners()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.handlePermanentChosen(player1, elvesId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Death damage may be declined after choosing a target")
    void deathDamageMayBeDeclined() {
        harness.addToBattlefield(player1, new JundSojourners());
        int lifeBefore = gd.getLife(player2.getId());
        killWithFlameJavelin();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cycling damage may be declined without preventing the separate draw")
    void cyclingDamageMayBeDeclined() {
        harness.setHand(player1, List.of(new JundSojourners()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateHandAbility(player1, 0, null);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An illegal cycling damage target does not prevent the separate draw")
    void cyclingStillDrawsWhenDamageTargetDies() {
        harness.setHand(player1, List.of(new JundSojourners()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.handlePermanentChosen(player1, elvesId);
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, elvesId);
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.passBothPriorities();
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void killWithFlameJavelin() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);

        UUID jundId = harness.getPermanentId(player1, "Jund Sojourners");
        harness.castAndResolveInstant(player2, 0, jundId);
    }
}
