package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DetectiveOfTheMonth.class, Forest.class, GrizzlyBears.class})
class DetectiveOfTheMonthTest extends BaseCardTest {

    @Test
    void secondDrawCreatesDetectiveTokenOncePerTurn() {
        addCreatureReady(player1, new DetectiveOfTheMonth());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        draw(player1.getId());
        draw(player1.getId());
        resolveTopOfStack();
        draw(player1.getId());

        assertThat(findPermanents(player1, "Detective")).hasSize(1);
        assertThat(findPermanents(player1, "Detective").getFirst().getCard().getSubtypes())
                .contains(CardSubtype.DETECTIVE);
    }

    @Test
    void detectiveCanBeBlockedWithoutCityBlessing() {
        Permanent detective = addCreatureReady(player1, new DetectiveOfTheMonth());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        detective.setAttacking(true);

        prepareDeclareBlockers();
        assertThatCode(() -> declareBlock(blocker, detective)).doesNotThrowAnyException();
    }

    @Test
    void cityBlessingMakesOwnDetectivesUnblockable() {
        Permanent detective = addCreatureReady(player1, new DetectiveOfTheMonth());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        detective.setAttacking(true);

        gd.playersWithCityBlessing.add(player1.getId());
        prepareDeclareBlockers();
        assertThatThrownBy(() -> declareBlock(blocker, detective))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ascendGrantsBlessingAtTenthPermanent() {
        addCreatureReady(player1, new DetectiveOfTheMonth());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }

    private void draw(java.util.UUID playerId) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, playerId));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
