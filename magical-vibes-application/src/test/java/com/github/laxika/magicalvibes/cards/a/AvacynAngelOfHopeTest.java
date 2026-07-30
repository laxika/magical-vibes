package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvacynAngelOfHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Other permanents you control have indestructible, creatures and noncreatures alike")
    void grantsIndestructibleToOtherOwnPermanents() {
        harness.addToBattlefield(player1, new AvacynAngelOfHope());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent forest = findPermanent(player1, "Forest");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Does not affect permanents an opponent controls")
    void doesNotAffectOpponentPermanents() {
        harness.addToBattlefield(player1, new AvacynAngelOfHope());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        Permanent forest = findPermanent(player2, "Forest");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Your creatures survive a board wipe while an opponent's do not")
    void protectedCreaturesSurviveWrath() {
        harness.addToBattlefield(player1, new AvacynAngelOfHope());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Avacyn, Angel of Hope");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
}
