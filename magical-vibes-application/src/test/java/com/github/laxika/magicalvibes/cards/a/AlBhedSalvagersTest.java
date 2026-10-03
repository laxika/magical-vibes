package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
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

@CardUsed({AlBhedSalvagers.class, GrizzlyBears.class, LightningBolt.class, MindStone.class,
        Naturalize.class, Ornithopter.class, Shock.class, WrathOfGod.class})
class AlBhedSalvagersTest extends BaseCardTest {

    @Test
    @DisplayName("When Al Bhed Salvagers dies, target opponent loses 1 life and its controller gains 1 life")
    void selfDeathDrainsTargetOpponent() {
        harness.addToBattlefield(player1, new AlBhedSalvagers());

        destroyWithLightningBolt(player1, "Al Bhed Salvagers");
        resolveDrain(player2.getId());

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("A non-artifact creature you control dying triggers Al Bhed Salvagers")
    void nonArtifactCreatureDeathDrainsTargetOpponent() {
        harness.addToBattlefield(player1, new AlBhedSalvagers());
        harness.addToBattlefield(player1, new GrizzlyBears());

        destroyWithShock(player1, "Grizzly Bears");
        resolveDrain(player2.getId());

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("An artifact you control being put into a graveyard triggers Al Bhed Salvagers")
    void artifactDeathDrainsTargetOpponent() {
        harness.addToBattlefield(player1, new AlBhedSalvagers());
        harness.addToBattlefield(player1, new MindStone());

        destroyArtifact(player1, "Mind Stone");
        resolveDrain(player2.getId());

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("An artifact creature dying triggers Al Bhed Salvagers only once")
    void artifactCreatureDeathDoesNotTriggerTwice() {
        harness.addToBattlefield(player1, new AlBhedSalvagers());
        harness.addToBattlefield(player1, new Ornithopter());

        destroyArtifact(player1, "Ornithopter");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        resolveDrain(player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("An opponent's creature dying does not trigger Al Bhed Salvagers")
    void opponentCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new AlBhedSalvagers());
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyWithShock(player2, "Grizzly Bears");

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's artifact dying does not trigger Al Bhed Salvagers")
    void opponentArtifactDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new AlBhedSalvagers());
        harness.addToBattlefield(player2, new MindStone());

        destroyArtifact(player2, "Mind Stone");

        harness.assertInGraveyard(player2, "Mind Stone");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Salvagers sees a non-artifact creature dying simultaneously with itself")
    void simultaneousNonArtifactCreatureDeathDrainsTwice() {
        harness.addToBattlefield(player1, new AlBhedSalvagers());
        harness.addToBattlefield(player1, new GrizzlyBears());

        destroyAllCreatures();
        resolveSimultaneousDrains();

        harness.assertInGraveyard(player1, "Al Bhed Salvagers");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Salvagers sees an artifact creature dying simultaneously with itself")
    void simultaneousArtifactCreatureDeathDrainsTwice() {
        harness.addToBattlefield(player1, new AlBhedSalvagers());
        harness.addToBattlefield(player1, new Ornithopter());

        destroyAllCreatures();
        resolveSimultaneousDrains();

        harness.assertInGraveyard(player1, "Al Bhed Salvagers");
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    private void destroyAllCreatures() {
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        setupPlayer2Active();
        harness.castAndResolveSorcery(player2, 0, 0);
    }

    private void resolveSimultaneousDrains() {
        for (int i = 0; i < 2; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, player2.getId());
        }
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void destroyWithShock(com.github.laxika.magicalvibes.model.Player controller, String permanentName) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        setupPlayer2Active();

        UUID permanentId = harness.getPermanentId(controller, permanentName);
        harness.castAndResolveInstant(player2, 0, permanentId);
    }

    private void destroyWithLightningBolt(com.github.laxika.magicalvibes.model.Player controller,
                                          String permanentName) {
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        setupPlayer2Active();

        UUID permanentId = harness.getPermanentId(controller, permanentName);
        harness.castAndResolveInstant(player2, 0, permanentId);
    }

    private void destroyArtifact(com.github.laxika.magicalvibes.model.Player controller, String permanentName) {
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        setupPlayer2Active();

        UUID permanentId = harness.getPermanentId(controller, permanentName);
        harness.castAndResolveInstant(player2, 0, permanentId);
    }

    private void resolveDrain(UUID targetId) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
