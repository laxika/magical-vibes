package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.BlasphemousAct;

import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiregrafCaptain.class, DiregrafGhoul.class, Gravedigger.class, GrizzlyBears.class,
        LightningBolt.class, Shock.class, BlasphemousAct.class, Xenograft.class})
class DiregrafCaptainTest extends BaseCardTest {


    @Test
    @DisplayName("Other Zombie creatures you control get +1/+1")
    void buffsOwnZombies() {
        harness.addToBattlefield(player1, new Gravedigger());
        harness.addToBattlefield(player1, new DiregrafCaptain());

        Permanent zombie = findPermanent(player1, "Gravedigger");

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(3);
    }

    @Test
    @DisplayName("Diregraf Captain does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new DiregrafCaptain());

        Permanent captain = findPermanent(player1, "Diregraf Captain");

        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Zombie creatures")
    void doesNotBuffNonZombies() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new DiregrafCaptain());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }


    @Test
    @DisplayName("When another Zombie you control dies, target opponent loses 1 life")
    void anotherZombieDeathDrainsOpponent() {
        harness.addToBattlefield(player1, new DiregrafCaptain());
        harness.addToBattlefield(player1, new DiregrafGhoul());

        int p2LifeBefore = gd.getLife(player2.getId());

        // Diregraf Ghoul is 2/2 base, 3/3 under the anthem — kill it with Lightning Bolt (3 damage).
        setupPlayer2Active();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID ghoulId = harness.getPermanentId(player1, "Diregraf Ghoul");
        harness.castAndResolveInstant(player2, 0, ghoulId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        // Only the opponent (player2) is a valid target, never the controller (player1).
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Resolve the life-loss trigger

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 1);
    }

    @Test
    @DisplayName("Does not trigger when a non-Zombie creature you control dies")
    void nonZombieDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new DiregrafCaptain());
        harness.addToBattlefield(player1, new GrizzlyBears());

        int p2LifeBefore = gd.getLife(player2.getId());

        // Grizzly Bears is not a Zombie, so the anthem doesn't apply — 2/2 dies to Shock.
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, bearsId);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore);
    }

    @Test
    @DisplayName("Diregraf Captain's own death does not trigger its ability (\"another\" Zombie)")
    void ownDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new DiregrafCaptain());

        int p2LifeBefore = gd.getLife(player2.getId());

        // Diregraf Captain is 2/2 (does not buff itself) — dies to Shock.
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID captainId = harness.getPermanentId(player1, "Diregraf Captain");
        harness.castAndResolveInstant(player2, 0, captainId);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore);
    }

    @Test
    @DisplayName("Does not buff or trigger for an opponent's Zombie")
    void opponentZombieIsUnaffected() {
        harness.addToBattlefield(player1, new DiregrafCaptain());
        harness.addToBattlefield(player2, new DiregrafGhoul());
        Permanent ghoul = findPermanent(player2, "Diregraf Ghoul");
        assertThat(gqs.getEffectivePower(gd, ghoul)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ghoul)).isEqualTo(2);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, ghoul.getId());

        harness.assertInGraveyard(player2, "Diregraf Ghoul");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Two Captains buff each other and their anthems stack")
    void multipleCaptainsStackAnthems() {
        harness.addToBattlefield(player1, new DiregrafCaptain());
        harness.addToBattlefield(player1, new DiregrafCaptain());
        harness.addToBattlefield(player1, new DiregrafGhoul());

        for (Permanent captain : gd.playerBattlefields.get(player1.getId()).subList(0, 2)) {
            assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(3);
        }
        Permanent ghoul = findPermanent(player1, "Diregraf Ghoul");
        assertThat(gqs.getEffectivePower(gd, ghoul)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ghoul)).isEqualTo(4);
    }

    @Test
    @DisplayName("Captain sees another Zombie die simultaneously with itself")
    void simultaneousDeathStillTriggers() {
        harness.addToBattlefield(player1, new DiregrafCaptain());
        harness.addToBattlefield(player1, new DiregrafGhoul());
        harness.setHand(player1, List.of(new BlasphemousAct()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Diregraf Captain");
        harness.assertInGraveyard(player1, "Diregraf Ghoul");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A creature made into a Zombie triggers using its battlefield type")
    void grantedZombieSubtypeCountsAtDeath() {
        harness.addToBattlefield(player1, new DiregrafCaptain());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Xenograft());
        findPermanent(player1, "Xenograft").setChosenSubtype(CardSubtype.ZOMBIE);
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
