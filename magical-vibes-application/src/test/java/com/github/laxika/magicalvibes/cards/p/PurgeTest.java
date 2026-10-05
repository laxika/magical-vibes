package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ArcboundWorker;
import com.github.laxika.magicalvibes.cards.a.AuriokGlaivemaster;
import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.e.EchoingTruth;
import com.github.laxika.magicalvibes.cards.n.NeurokTransmuter;
import com.github.laxika.magicalvibes.cards.n.NimAbomination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Purge.class, ArcboundWorker.class, AuriokGlaivemaster.class, DarksteelGargoyle.class,
        DarksteelIngot.class, NimAbomination.class, EchoingTruth.class, NeurokTransmuter.class})
class PurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Purge destroys a target artifact creature")
    void destroysArtifactCreature() {
        Permanent worker = harness.enterBattlefieldAndReturn(player2, new ArcboundWorker());
        harness.setHand(player1, List.of(new Purge()));
        addPurgeMana();

        harness.castAndResolveInstant(player1, 0, worker.getId());

        harness.assertNotOnBattlefield(player2, "Arcbound Worker");
        harness.assertInGraveyard(player2, "Arcbound Worker");
    }

    @Test
    @DisplayName("Purge does not destroy an indestructible artifact creature")
    void doesNotDestroyIndestructibleArtifactCreature() {
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player2, new DarksteelGargoyle());
        harness.setHand(player1, List.of(new Purge()));
        addPurgeMana();

        harness.castAndResolveInstant(player1, 0, gargoyle.getId());

        harness.assertOnBattlefield(player2, "Darksteel Gargoyle");
        harness.assertNotInGraveyard(player2, "Darksteel Gargoyle");
    }

    @Test
    @DisplayName("Purge destroys a target black creature and ignores regeneration")
    void destroysBlackCreatureWithoutRegeneration() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player2, new NimAbomination());
        abomination.setRegenerationShield(1);
        harness.setHand(player1, List.of(new Purge()));
        addPurgeMana();

        harness.castAndResolveInstant(player1, 0, abomination.getId());

        harness.assertNotOnBattlefield(player2, "Nim Abomination");
        harness.assertInGraveyard(player2, "Nim Abomination");
    }

    @Test
    @DisplayName("Purge cannot target a noncreature artifact")
    void cannotTargetNoncreatureArtifact() {
        Permanent ingot = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        harness.setHand(player1, List.of(new Purge()));
        addPurgeMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ingot.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact creature or black creature");
    }

    @Test
    @DisplayName("Purge cannot target a nonblack nonartifact creature")
    void cannotTargetNonblackNonartifactCreature() {
        Permanent glaivemaster = harness.addToBattlefieldAndReturn(player2, new AuriokGlaivemaster());
        harness.setHand(player1, List.of(new Purge()));
        addPurgeMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, glaivemaster.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact creature or black creature");
    }

    @Test
    @DisplayName("Purge can destroy its controller's black creature")
    void destroysOwnBlackCreature() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new NimAbomination());
        harness.setHand(player1, List.of(new Purge()));
        addPurgeMana();

        harness.castAndResolveInstant(player1, 0, abomination.getId());

        harness.assertNotOnBattlefield(player1, "Nim Abomination");
        harness.assertInGraveyard(player1, "Nim Abomination");
    }

    @Test
    @DisplayName("Purge also prevents regeneration of a nonblack artifact creature")
    void destroysArtifactCreatureWithoutRegeneration() {
        Permanent worker = harness.enterBattlefieldAndReturn(player2, new ArcboundWorker());
        worker.setRegenerationShield(1);
        harness.setHand(player1, List.of(new Purge()));
        addPurgeMana();

        harness.castAndResolveInstant(player1, 0, worker.getId());

        harness.assertNotOnBattlefield(player2, "Arcbound Worker");
        harness.assertInGraveyard(player2, "Arcbound Worker");
    }

    @Test
    @DisplayName("Purge does not affect a target returned to hand in response")
    void targetReturnedToHandBeforeResolution() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player2, new NimAbomination());
        harness.setHand(player1, List.of(new Purge()));
        harness.setHand(player2, List.of(new EchoingTruth()));
        addPurgeMana();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, abomination.getId());
        harness.castAndResolveInstant(player2, 0, abomination.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Nim Abomination");
        harness.assertNotInGraveyard(player2, "Nim Abomination");
        harness.assertInGraveyard(player1, "Purge");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Purge rechecks artifact status when resolving")
    void targetBecomesNonblackNonartifactBeforeResolution() {
        harness.addToBattlefield(player2, new NeurokTransmuter());
        Permanent worker = harness.enterBattlefieldAndReturn(player2, new ArcboundWorker());
        harness.setHand(player1, List.of(new Purge()));
        addPurgeMana();
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, worker.getId());
        harness.activateAbility(player2, 0, 1, null, worker.getId());
        harness.passBothPriorities();
        assertThat(gqs.isArtifact(gd, worker)).isFalse();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Arcbound Worker");
        harness.assertNotInGraveyard(player2, "Arcbound Worker");
        harness.assertInGraveyard(player1, "Purge");
        assertThat(gd.stack).isEmpty();
    }

    private void addPurgeMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
