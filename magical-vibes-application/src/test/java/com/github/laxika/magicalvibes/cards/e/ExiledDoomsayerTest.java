package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExiledDoomsayer.class, ScornfulEgotist.class})
class ExiledDoomsayerTest extends BaseCardTest {

    @Test
    void increasesMorphCostWithoutIncreasingFaceDownCastCost() {
        harness.addToBattlefield(player1, new ExiledDoomsayer());
        harness.setHand(player1, List.of(new ScornfulEgotist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent egotist = findPermanent(player1, "Scornful Egotist");
        assertThat(egotist.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(egotist)))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(egotist));

        assertThat(egotist.isFaceDown()).isFalse();
    }

    @Test
    void increasesOpponentsMorphCost() {
        harness.addToBattlefield(player1, new ExiledDoomsayer());
        harness.setHand(player2, List.of(new ScornfulEgotist()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        harness.castCreatureWithMorph(player2, 0);
        resolveAllTriggers();

        Permanent egotist = findPermanent(player2, "Scornful Egotist");
        assertThat(egotist.isFaceDown()).isTrue();

        harness.addMana(player2, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.turnFaceUp(
                player2, gd.playerBattlefields.get(player2.getId()).indexOf(egotist)))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player2, gd.playerBattlefields.get(player2.getId()).indexOf(egotist));

        assertThat(egotist.isFaceDown()).isFalse();
    }
}
