package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
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

@CardUsed({SkirkShaman.class, GrizzlyBears.class, RagingGoblin.class, Ornithopter.class})
class SkirkShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Skirk Shaman cannot be blocked by a non-artifact, non-red creature")
    void cannotBeBlockedByNonArtifactNonRedCreature() {
        Permanent shaman = addReadyPermanent(player1, new SkirkShaman(), true);
        Permanent blocker = addReadyPermanent(player2, new GrizzlyBears(), false);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlocker(blocker, shaman))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact creatures or red creatures");
    }

    @Test
    @DisplayName("Skirk Shaman can be blocked by a red creature")
    void canBeBlockedByRedCreature() {
        Permanent shaman = addReadyPermanent(player1, new SkirkShaman(), true);
        Permanent blocker = addReadyPermanent(player2, new RagingGoblin(), false);

        prepareDeclareBlockers();
        declareBlocker(blocker, shaman);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Skirk Shaman can be blocked by an artifact creature")
    void canBeBlockedByArtifactCreature() {
        Permanent shaman = addReadyPermanent(player1, new SkirkShaman(), true);
        Permanent blocker = addReadyPermanent(player2, new Ornithopter(), false);

        prepareDeclareBlockers();
        declareBlocker(blocker, shaman);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void declareBlocker(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker))));
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
