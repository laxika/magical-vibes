package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SongOfCreation.class, Forest.class, Opt.class})
class SongOfCreationTest extends BaseCardTest {

    @Test
    @DisplayName("Controller may play one additional land each turn")
    void grantsControllerAnAdditionalLandPlay() {
        harness.addToBattlefield(player1, new SongOfCreation());
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Forest".equals(permanent.getCard().getName())))
                .hasSize(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a spell draws two cards")
    void drawsTwoCardsWhenControllerCastsASpell() {
        harness.addToBattlefield(player1, new SongOfCreation());
        harness.setHand(player1, List.of(new Opt()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Controller discards their hand at their end step")
    void discardsHandAtControllerEndStep() {
        harness.addToBattlefield(player1, new SongOfCreation());
        harness.setHand(player1, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }
}
