package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelJiladArchersTest extends BaseCardTest {

    @Test
    @DisplayName("Reach lets Tel-Jilad Archers block a creature with flying")
    void reachCanBlockFlyer() {
        Permanent flyer = addReadyPermanent(player1, new SuntailHawk(), true);
        Permanent archer = addReadyPermanent(player2, new TelJiladArchers(), false);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, archer), indexOf(player1, flyer))));

        assertThat(archer.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Protection from artifacts prevents artifact creatures from blocking")
    void artifactCreatureCannotBlockArchers() {
        Permanent archer = addReadyPermanent(player1, new TelJiladArchers(), true);
        Permanent artifactCreature = addReadyPermanent(player2, new Ornithopter(), false);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, artifactCreature), indexOf(player1, archer)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from artifacts does not prevent non-artifact creatures from blocking")
    void nonArtifactCreatureCanBlockArchers() {
        Permanent archer = addReadyPermanent(player1, new TelJiladArchers(), true);
        Permanent creature = addReadyPermanent(player2, new GrizzlyBears(), false);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, creature), indexOf(player1, archer))));

        assertThat(creature.isBlocking()).isTrue();
    }

    private Permanent addReadyPermanent(Player player, Card card, boolean attacking) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(attacking);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
