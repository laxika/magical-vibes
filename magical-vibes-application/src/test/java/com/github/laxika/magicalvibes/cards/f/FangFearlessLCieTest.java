package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FangFearlessLCie.class, Disentomb.class, Forest.class, GrizzlyBears.class})
class FangFearlessLCieTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and loses 1 life when a card leaves its graveyard")
    void drawsAndLosesLifeWhenCardLeavesGraveyard() {
        addFang();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Forest");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        addFang();
        GrizzlyBears firstBears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstBears, secondBears));
        harness.setHand(player1, List.of(new Disentomb(), new Disentomb()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, firstBears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castSorcery(player1, 0, secondBears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Triggers again on a later turn")
    void triggersAgainOnLaterTurn() {
        addFang();
        GrizzlyBears firstBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstBears));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, firstBears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceTurn();
        advanceTurn();

        GrizzlyBears secondBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(secondBears));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, secondBears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Forest");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    private void addFang() {
        harness.addToBattlefield(player1, new FangFearlessLCie());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
