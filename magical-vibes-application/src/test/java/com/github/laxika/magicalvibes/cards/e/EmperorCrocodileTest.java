package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NaturalAffinity;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmperorCrocodile.class, GrizzlyBears.class, DarkBanishing.class,
        NaturalAffinity.class, Forest.class})
class EmperorCrocodileTest extends BaseCardTest {

    @Test
    @DisplayName("Cast with no other creatures — state trigger fires and Crocodile is sacrificed")
    void sacrificedWhenControllingNoOtherCreatures() {
        harness.castFromHand(player1, new EmperorCrocodile(), "{3}{G}");
        harness.passBothPriorities(); // resolve creature spell → state trigger fires

        // State trigger is on the stack — Crocodile still alive
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.assertOnBattlefield(player1, "Emperor Crocodile");

        // Resolve state trigger → Crocodile is sacrificed
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Emperor Crocodile");
        harness.assertInGraveyard(player1, "Emperor Crocodile");
    }

    @Test
    @DisplayName("Sacrifice still resolves after another creature enters")
    void sacrificeStillResolvesAfterAnotherCreatureEnters() {
        harness.castFromHand(player1, new EmperorCrocodile(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Emperor Crocodile");
        harness.assertInGraveyard(player1, "Emperor Crocodile");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counts another Emperor Crocodile as an other creature")
    void countsAnotherCopyAsOtherCreature() {
        harness.castFromHand(player1, new EmperorCrocodile(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.addToBattlefield(player1, new EmperorCrocodile());
        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Emperor Crocodile")).isEqualTo(1);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Emperor Crocodile")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Survives while controlling another creature — no state trigger")
    void survivesWithAnotherCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new EmperorCrocodile(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Emperor Crocodile");
    }

    @Test
    @DisplayName("Sacrificed when the last other creature dies")
    void sacrificedWhenLastOtherCreatureDies() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new EmperorCrocodile(), "{3}{G}");
        harness.passBothPriorities();

        // Both creatures present, no trigger yet
        assertThat(gd.stack).isEmpty();

        // Destroy the Bear with Dark Banishing.
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player2, List.of(new DarkBanishing()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, bearId);

        // Bear gone → state trigger fires; resolve it → Crocodile sacrificed
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Emperor Crocodile");
        harness.assertInGraveyard(player1, "Emperor Crocodile");
    }

    @Test
    @DisplayName("Controlling another creature owned via opponent does not count — sacrificed")
    void opponentCreaturesDoNotCount() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new EmperorCrocodile(), "{3}{G}");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Emperor Crocodile");
        harness.assertInGraveyard(player1, "Emperor Crocodile");
    }

    @Test
    @DisplayName("Survives while another noncreature permanent is effectively a creature")
    void survivesWithEffectivelyCreaturePermanent() {
        harness.addToBattlefield(player1, new Forest());
        harness.castFromHand(player1, new NaturalAffinity(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, findPermanent(player1, "Forest"))).isTrue();
        harness.addToBattlefield(player1, new EmperorCrocodile());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Emperor Crocodile");
    }
}
