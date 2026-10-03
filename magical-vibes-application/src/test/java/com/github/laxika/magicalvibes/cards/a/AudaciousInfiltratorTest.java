package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AudaciousInfiltrator.class, GrizzlyBears.class, IronMyr.class})
class AudaciousInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Audacious Infiltrator can't be blocked by an artifact creature")
    void cannotBeBlockedByArtifactCreature() {
        Permanent infiltrator = addReadyPermanent(player1, new AudaciousInfiltrator(), true);
        Permanent artifactCreature = addReadyPermanent(player2, new IronMyr(), false);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, artifactCreature), indexOf(player1, infiltrator)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Audacious Infiltrator can be blocked by a non-artifact creature")
    void canBeBlockedByNonArtifactCreature() {
        Permanent infiltrator = addReadyPermanent(player1, new AudaciousInfiltrator(), true);
        Permanent creature = addReadyPermanent(player2, new GrizzlyBears(), false);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, creature), indexOf(player1, infiltrator))));

        assertThat(creature.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Audacious Infiltrator can block an attacking artifact creature")
    void canBlockArtifactCreature() {
        Permanent attacker = addReadyPermanent(player1, new IronMyr(), true);
        Permanent infiltrator = addReadyPermanent(player2, new AudaciousInfiltrator(), false);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, infiltrator), indexOf(player1, attacker))));

        assertThat(infiltrator.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Audacious Infiltrator does not prevent artifact creatures from blocking other attackers")
    void otherAttackerCanBeBlockedByArtifactCreature() {
        addReadyPermanent(player1, new AudaciousInfiltrator(), true);
        Permanent attacker = addReadyPermanent(player1, new GrizzlyBears(), true);
        Permanent blocker = addReadyPermanent(player2, new IronMyr(), false);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addReadyPermanent(Player player, Card card, boolean attacking) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setAttacking(attacking);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
