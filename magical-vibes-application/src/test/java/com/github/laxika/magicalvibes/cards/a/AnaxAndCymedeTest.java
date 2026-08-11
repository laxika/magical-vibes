package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnaxAndCymedeTest extends BaseCardTest {

    @Test
    @DisplayName("Heroic gives your creatures +1/+1 and trample")
    void heroicBoostsOwnCreaturesAndGrantsTrample() {
        Permanent anax = harness.addToBattlefieldAndReturn(player1, new AnaxAndCymede());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, anax.getId());
        harness.passBothPriorities();

        assertThat(anax.getPowerModifier()).isEqualTo(1);
        assertThat(anax.getToughnessModifier()).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, anax, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(opponentBears.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Heroic bonuses wear off at end of turn")
    void heroicBonusesWearOffAtEndOfTurn() {
        Permanent anax = harness.addToBattlefieldAndReturn(player1, new AnaxAndCymede());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, anax.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A spell targeting a player does not trigger heroic")
    void playerTargetDoesNotTriggerHeroic() {
        Permanent anax = harness.addToBattlefieldAndReturn(player1, new AnaxAndCymede());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(anax.getPowerModifier()).isEqualTo(0);
        assertThat(anax.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, anax, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's spell targeting Anax and Cymede does not trigger heroic")
    void opponentSpellDoesNotTriggerHeroic() {
        Permanent anax = harness.addToBattlefieldAndReturn(player1, new AnaxAndCymede());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, anax.getId());
        harness.passBothPriorities();

        assertThat(anax.getPowerModifier()).isEqualTo(0);
        assertThat(anax.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, anax, Keyword.TRAMPLE)).isFalse();
    }
}
