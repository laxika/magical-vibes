package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChardalynDragon.class, GoblinPiker.class})
class ChardalynDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Chardalyn Dragon")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent dragon = addCreatureReady(player1, new ChardalynDragon());
        addCreatureReady(player2, new GoblinPiker());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dragon);
    }

    @Test
    @DisplayName("Flying allows Chardalyn Dragon to block another flying creature")
    void canBeBlockedByFlyingCreature() {
        addCreatureReady(player1, new ChardalynDragon());
        Permanent blocker = addCreatureReady(player2, new ChardalynDragon());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
