package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JeskaiInfiltratorTest extends BaseCardTest {

    @Test
    void cannotBeBlockedWhileItIsYourOnlyCreature() {
        Permanent infiltrator = addReadyInfiltrator();
        infiltrator.setAttacking(true);
        addReadyBlocker();

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void canBeBlockedWhenYouControlAnotherCreature() {
        Permanent infiltrator = addReadyInfiltrator();
        infiltrator.setAttacking(true);
        addReadyInfiltrator();
        addReadyBlocker();

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    void combatDamageExilesAndManifestsSourceAndTopCard() {
        Permanent infiltrator = addReadyInfiltrator();
        infiltrator.setAttacking(true);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2);
        assertThat(battlefield).allMatch(permanent -> permanent.isFaceDown() && permanent.isManifested());
        assertThat(battlefield).anyMatch(permanent -> permanent.getCard().getId().equals(infiltrator.getCard().getId()));
        assertThat(battlefield).anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyInfiltrator() {
        Permanent permanent = new Permanent(new JeskaiInfiltrator());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyBlocker() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        return blocker;
    }
}
