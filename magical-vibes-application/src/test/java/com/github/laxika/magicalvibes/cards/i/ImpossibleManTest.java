package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImpossibleMan.class, Forest.class, GrizzlyBears.class})
class ImpossibleManTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a copy of another permanent while keeping its name")
    void becomesCopyOfAnotherPermanentWithNameException() {
        Permanent impossibleMan = harness.addToBattlefieldAndReturn(player1, new ImpossibleMan());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(impossibleMan.getCard().getName()).isEqualTo("Impossible Man");
        assertThat(impossibleMan.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(impossibleMan.getCard().hasType(CardType.CREATURE)).isFalse();
    }

    @Test
    @DisplayName("Copy reverts at end of turn")
    void copyRevertsAtEndOfTurn() {
        Permanent impossibleMan = harness.addToBattlefieldAndReturn(player1, new ImpossibleMan());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int originalToughness = impossibleMan.getCard().getToughness();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(impossibleMan.getCard().getToughness()).isEqualTo(bears.getCard().getToughness());
        assertThat(impossibleMan.getCard().getToughness()).isNotEqualTo(originalToughness);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(impossibleMan.getCard().getToughness()).isEqualTo(bears.getCard().getToughness());
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(impossibleMan.getCard().getName()).isEqualTo("Impossible Man");
        assertThat(impossibleMan.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(impossibleMan.getCard().getToughness()).isEqualTo(originalToughness);
    }

    @Test
    @DisplayName("Can copy an opponent's permanent without changing controller")
    void copiesOpponentsPermanent() {
        Permanent impossibleMan = harness.addToBattlefieldAndReturn(player1, new ImpossibleMan());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(impossibleMan.getCard().getName()).isEqualTo("Impossible Man");
        assertThat(impossibleMan.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(impossibleMan);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(forest).doesNotContain(impossibleMan);
    }

    @Test
    @DisplayName("Does not copy a target that leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent impossibleMan = harness.addToBattlefieldAndReturn(player1, new ImpossibleMan());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, forest.getId());
        gd.playerBattlefields.get(player2.getId()).remove(forest);
        gd.playerGraveyards.get(player2.getId()).add(forest.getCard());
        harness.passBothPriorities();

        assertThat(impossibleMan.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(impossibleMan.getCard().hasType(CardType.LAND)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetItself() {
        Permanent impossibleMan = harness.addToBattlefieldAndReturn(player1, new ImpossibleMan());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, impossibleMan.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another permanent");
    }
}
