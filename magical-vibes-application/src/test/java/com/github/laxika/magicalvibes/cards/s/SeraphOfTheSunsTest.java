package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.f.FinalReward;
import com.github.laxika.magicalvibes.cards.n.NeverReturn;
import com.github.laxika.magicalvibes.cards.r.Return;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeraphOfTheSuns.class, Colossapede.class, FinalReward.class,
        NeverReturn.class, Return.class, SplendidAgony.class})
class SeraphOfTheSunsTest extends BaseCardTest {

    @Test
    void groundCreatureCannotBlock() {
        addCreatureReady(player1, new SeraphOfTheSuns());
        addCreatureReady(player2, new Colossapede());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new SeraphOfTheSuns());
        Permanent blocker = addCreatureReady(player2, new SeraphOfTheSuns());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void survivesLethalCombatDamage() {
        Permanent seraph = addCreatureReady(player2, new SeraphOfTheSuns());
        addCreatureReady(player1, new Colossapede());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();

        assertThat(seraph.getMarkedDamage()).isEqualTo(5);
        harness.assertOnBattlefield(player2, "Seraph of the Suns");
        harness.assertNotInGraveyard(player2, "Seraph of the Suns");
    }

    @Test
    void destroySpellDoesNotDestroyIt() {
        Permanent seraph = harness.addToBattlefieldAndReturn(player2, new SeraphOfTheSuns());
        harness.setHand(player1, List.of(new NeverReturn()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, seraph.getId());

        harness.assertOnBattlefield(player2, "Seraph of the Suns");
        harness.assertNotInGraveyard(player2, "Seraph of the Suns");
        harness.assertInGraveyard(player1, "Never");
    }

    @Test
    void exileSpellStillExilesIt() {
        Permanent seraph = harness.addToBattlefieldAndReturn(player2, new SeraphOfTheSuns());
        harness.setHand(player1, List.of(new FinalReward()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, seraph.getId());

        harness.assertNotOnBattlefield(player2, "Seraph of the Suns");
        harness.assertNotInGraveyard(player2, "Seraph of the Suns");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(seraph.getCard());
    }

    @Test
    void zeroToughnessStillPutsItInGraveyard() {
        Permanent seraph = harness.addToBattlefieldAndReturn(player2, new SeraphOfTheSuns());
        harness.setHand(player1, List.of(new SplendidAgony(), new SplendidAgony()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of(seraph.getId()));
        harness.assertOnBattlefield(player2, "Seraph of the Suns");
        harness.castAndResolveInstant(player1, 0, List.of(seraph.getId()));

        harness.assertNotOnBattlefield(player2, "Seraph of the Suns");
        harness.assertInGraveyard(player2, "Seraph of the Suns");
    }
}
