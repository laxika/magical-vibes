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

        assertThat(targetCreature.getSkipUntapCount()).isEqualTo(1);

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
        harness.addToBattlefield(player2, new MercadianAtlas());
        Permanent targetArtifact = gd.playerBattlefields.get(player2.getId()).getFirst();
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

        assertThat(ownCreature.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target an object that is not a player")
    void requiresPlayerTarget() {
        harness.setHand(player1, List.of(new Misstep()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMisstep(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Misstep()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }
}
