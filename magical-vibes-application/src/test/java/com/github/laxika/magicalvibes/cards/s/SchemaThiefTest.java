package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SchemaThief.class, HowlingMine.class, GrizzlyBears.class})
class SchemaThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage creates a token copy of an artifact controlled by the damaged player")
    void createsTokenCopyOfDamagedPlayersArtifact() {
        Permanent thief = addCreatureReady(player1, new SchemaThief());
        thief.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new HowlingMine());

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(artifact.getId());

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Howling Mine")).hasSize(1);
        assertThat(findPermanents(player1, "Howling Mine")).allMatch(p -> p.getCard().isToken());
        assertThat(findPermanents(player2, "Howling Mine")).hasSize(1);
    }

    @Test
    @DisplayName("The combat trigger only permits artifacts controlled by the damaged player")
    void onlyDamagedPlayersArtifactsAreValidTargets() {
        Permanent thief = addCreatureReady(player1, new SchemaThief());
        thief.setAttacking(true);
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        Permanent damagedPlayersArtifact = harness.addToBattlefieldAndReturn(player2, new HowlingMine());
        Permanent damagedPlayersCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds())
                .containsExactly(damagedPlayersArtifact.getId())
                .doesNotContain(ownArtifact.getId(), damagedPlayersCreature.getId());
    }

    @Test
    @DisplayName("No combat trigger is put on the stack without an artifact to target")
    void doesNotTriggerWithoutArtifact() {
        Permanent thief = addCreatureReady(player1, new SchemaThief());
        thief.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
