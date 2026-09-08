package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Bogstomper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
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

@CardUsed({FreneticRaptor.class, Bogstomper.class, GrizzlyBears.class})
class FreneticRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Beasts can't block while Frenetic Raptor is on the battlefield")
    void beastsCannotBlock() {
        addReadyFreneticRaptor(player1);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player2, new Bogstomper());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Beasts can't block");
    }

    @Test
    @DisplayName("A non-Beast creature can still block")
    void nonBeastCanBlock() {
        addReadyFreneticRaptor(player1);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }

    private Permanent addReadyFreneticRaptor(Player player) {
        Permanent permanent = new Permanent(new FreneticRaptor());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
