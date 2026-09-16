package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Throatseeker.class, GrizzlyBears.class})
class ThroatseekerTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked attacking Ninjas you control have lifelink")
    void unblockedAttackingNinjasHaveLifelink() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent firstNinja = addCreatureReady(player1, new Throatseeker());
        Permanent secondNinja = addCreatureReady(player1, new Throatseeker());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0, 1, 2));
        assertThat(gqs.hasKeyword(gd, firstNinja, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondNinja, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isFalse();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.getLife(player1.getId())).isEqualTo(26);
        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Blocked Ninjas do not have lifelink")
    void blockedNinjasDoNotHaveLifelink() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent ninja = addCreatureReady(player1, new Throatseeker());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
