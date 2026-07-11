package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BattlewandOak;
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

class TimberProtectorTest extends BaseCardTest {

    // ===== +1/+1 and indestructible to other Treefolk creatures =====

    @Test
    @DisplayName("Other Treefolk creatures you control get +1/+1 and indestructible")
    void buffsOtherTreefolk() {
        harness.addToBattlefield(player1, new BattlewandOak());
        Permanent oak = findPermanent(player1, "Battlewand Oak");
        int basePower = gqs.getEffectivePower(gd, oak);
        int baseToughness = gqs.getEffectiveToughness(gd, oak);

        harness.addToBattlefield(player1, new TimberProtector());

        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(baseToughness + 1);
        assertThat(gqs.hasKeyword(gd, oak, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Forests you control have indestructible but get no +1/+1")
    void grantsIndestructibleToForests() {
        harness.addToBattlefield(player1, new TimberProtector());
        harness.addToBattlefield(player1, new Forest());

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Timber Protector does not buff or protect itself")
    void doesNotAffectItself() {
        harness.addToBattlefield(player1, new TimberProtector());

        Permanent protector = findPermanent(player1, "Timber Protector");
        // "Other Treefolk" excludes the source.
        assertThat(gqs.hasKeyword(gd, protector, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Does not affect non-Treefolk creatures")
    void doesNotAffectNonTreefolk() {
        harness.addToBattlefield(player1, new TimberProtector());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Does not affect an opponent's Treefolk or Forests")
    void doesNotAffectOpponentPermanents() {
        harness.addToBattlefield(player1, new TimberProtector());
        harness.addToBattlefield(player2, new BattlewandOak());
        harness.addToBattlefield(player2, new Forest());

        Permanent opponentOak = findPermanent(player2, "Battlewand Oak");
        Permanent opponentForest = findPermanent(player2, "Forest");
        assertThat(gqs.hasKeyword(gd, opponentOak, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentForest, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    // ===== Indestructible prevents destruction =====

    @Test
    @DisplayName("Protected Treefolk survives Wrath of God while Timber Protector dies")
    void protectedTreefolkSurvivesWrath() {
        harness.addToBattlefield(player1, new TimberProtector());
        harness.addToBattlefield(player1, new BattlewandOak());

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        // Battlewand Oak is indestructible from the Protector and survives.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Battlewand Oak"));
        // Timber Protector does not protect itself and is destroyed.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Timber Protector"));
    }
}
