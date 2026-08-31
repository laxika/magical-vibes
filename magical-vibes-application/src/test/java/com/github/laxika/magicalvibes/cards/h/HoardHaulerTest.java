package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HoardHauler.class, LeoninScimitar.class, Ornithopter.class, GrizzlyBears.class})
class HoardHaulerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure for each artifact controlled by the damaged player")
    void createsTreasureForEachArtifactControlledByDamagedPlayer() {
        Permanent hauler = addPermanent(player1, new HoardHauler());
        hauler.setAnimatedUntilEndOfTurn(true);
        hauler.setAnimatedPower(5);
        hauler.setAnimatedToughness(5);
        hauler.setAttacking(true);

        addPermanent(player2, new LeoninScimitar());
        addPermanent(player2, new Ornithopter());
        addPermanent(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Does not create Treasures when blocked")
    void doesNotCreateTreasureWhenBlocked() {
        Permanent hauler = addPermanent(player1, new HoardHauler());
        hauler.setAnimatedUntilEndOfTurn(true);
        hauler.setAnimatedPower(5);
        hauler.setAnimatedToughness(5);
        hauler.setAttacking(true);

        addPermanent(player2, new LeoninScimitar());
        Permanent blocker = addPermanent(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private Permanent addPermanent(com.github.laxika.magicalvibes.model.Player player,
                                   com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
