package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WastelandScorpion.class, Colossapede.class})
class WastelandScorpionTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling {2} discards Wasteland Scorpion and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new WastelandScorpion()));
        harness.setLibrary(player1, List.of(new Colossapede()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Wasteland Scorpion");
        harness.assertInHand(player1, "Colossapede");
    }

    @Test
    void cyclingPaysManaAndDiscardsBeforeDrawing() {
        harness.setHand(player1, List.of(new WastelandScorpion()));
        harness.setLibrary(player1, List.of(new Colossapede()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Wasteland Scorpion");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Colossapede");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Wasteland Scorpion");
    }

    @Test
    void cyclingCannotBeActivatedWithOnlyOneMana() {
        harness.setHand(player1, List.of(new WastelandScorpion()));
        harness.setLibrary(player1, List.of(new Colossapede()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Wasteland Scorpion");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void deathtouchKillsLargerAttacker() {
        addCreatureReady(player1, new Colossapede());
        addCreatureReady(player2, new WastelandScorpion());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Colossapede");
        harness.assertInGraveyard(player2, "Wasteland Scorpion");
        harness.assertNotOnBattlefield(player1, "Colossapede");
        harness.assertNotOnBattlefield(player2, "Wasteland Scorpion");
        harness.assertLife(player2, 20);
    }

    @Test
    void unblockedScorpionDealsNormalDamageToPlayer() {
        addCreatureReady(player1, new WastelandScorpion());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Wasteland Scorpion");
    }
}
