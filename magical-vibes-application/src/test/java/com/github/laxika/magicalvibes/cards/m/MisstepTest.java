package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Misstep.class, DeadlyInsect.class, MercadianAtlas.class})
class MisstepTest extends BaseCardTest {

    @Test
    @DisplayName("Locks all creatures the target player controls through their next untap step")
    void locksTargetPlayersCreatures() {
        Permanent targetCreature = addCreatureReady(player2, new DeadlyInsect());
        targetCreature.tap();

        castMisstep(player2.getId());


        advanceToUpkeep(player2);
        assertThat(targetCreature.isTapped()).isTrue();

        advanceToUpkeep(player1);
        advanceToUpkeep(player2);
        assertThat(targetCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Also locks a creature that enters before the target player's next untap step")
    void locksCreatureEnteringBeforeNextUntapStep() {
        castMisstep(player2.getId());

        Permanent creatureEnteringAfterResolution = addCreatureReady(player2, new DeadlyInsect());
        creatureEnteringAfterResolution.tap();

        advanceToUpkeep(player2);

        assertThat(creatureEnteringAfterResolution.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not affect non-creatures or creatures controlled by another player")
    void affectsOnlyTargetPlayersCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new DeadlyInsect());
        Permanent targetArtifact = harness.addToBattlefieldAndReturn(player2, new MercadianAtlas());
        ownCreature.tap();
        targetArtifact.tap();

        castMisstep(player2.getId());

        assertThat(ownCreature.getSkipUntapCount()).isZero();
        assertThat(targetArtifact.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Can target the caster")
    void canTargetSelf() {
        Permanent ownCreature = addCreatureReady(player1, new DeadlyInsect());
        ownCreature.tap();

        castMisstep(player1.getId());

        advanceToUpkeep(player1);
        assertThat(ownCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target an object that is not a player")
    void requiresPlayerTarget() {
        harness.setHand(player1, List.of(new Misstep()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-creatures and the other player's creatures untap normally")
    void unaffectedPermanentsUntapNormally() {
        Permanent ownCreature = addCreatureReady(player1, new DeadlyInsect());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MercadianAtlas());
        ownCreature.tap();
        artifact.tap();

        castMisstep(player2.getId());

        advanceToUpkeep(player1);
        assertThat(ownCreature.isTapped()).isFalse();
        advanceToUpkeep(player2);
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Two Missteps before the same untap step do not lock a second untap step")
    void overlappingRestrictionsExpireTogether() {
        Permanent creature = addCreatureReady(player2, new DeadlyInsect());
        creature.tap();

        castMisstep(player2.getId());
        castMisstep(player2.getId());

        advanceToUpkeep(player2);
        assertThat(creature.isTapped()).isTrue();
        advanceToUpkeep(player1);
        advanceToUpkeep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not tap an untapped creature and still locks it if it taps later")
    void doesNotTapCreaturesOnResolution() {
        Permanent creature = addCreatureReady(player2, new DeadlyInsect());

        castMisstep(player2.getId());

        assertThat(creature.isTapped()).isFalse();
        creature.tap();
        advanceToUpkeep(player2);
        assertThat(creature.isTapped()).isTrue();
    }

    private void castMisstep(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Misstep()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }
}
