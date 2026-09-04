package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrubsCommand.class, GoblinSappers.class, BalduvianBears.class, Forest.class, Island.class})
class GrubsCommandTest extends BaseCardTest {

    @Test
    void copyAndDestroyModesResolveAgainstTheSameGoblin() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinSappers());
        harness.setHand(player1, List.of(new GrubsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2},
                List.of(goblin.getId(), goblin.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(goblin.getId()));
        harness.assertInGraveyard(player1, "Goblin Sappers");
    }

    @Test
    void boostAndMillModesAffectOnlyTheTargetPlayerAndReturnMilledGoblins() {
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        GoblinSappers firstGoblin = new GoblinSappers();
        GoblinSappers secondGoblin = new GoblinSappers();
        Forest forest = new Forest();
        Island island = new Island();
        BalduvianBears nonGoblin = new BalduvianBears();
        harness.setLibrary(player2, List.of(firstGoblin, forest, secondGoblin, island, nonGoblin));
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new GrubsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 3},
                List.of(player2.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, targetCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstGoblin, secondGoblin);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(forest, island, nonGoblin);
    }

    @Test
    void copyModeRejectsANonGoblinPermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new GrubsCommand()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2},
                List.of(forest.getId(), forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
