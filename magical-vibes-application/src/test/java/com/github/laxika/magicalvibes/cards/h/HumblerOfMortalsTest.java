package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HumblerOfMortalsTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry gives your creatures trample until end of turn")
    void ownEntryGrantsTrampleToYourCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castHumblerOfMortals();
        harness.passBothPriorities();
        Permanent humbler = findHumbler();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, humbler, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Another enchantment entering under your control grants your creatures trample")
    void anotherEnchantmentEntryGrantsTrample() {
        Permanent humbler = harness.addToBattlefieldAndReturn(player1, new HumblerOfMortals());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, humbler, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's enchantment entering does not trigger it")
    void opponentEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new HumblerOfMortals());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Trample wears off at the end of the turn")
    void trampleWearsOffAtEndOfTurn() {
        Permanent humbler = harness.addToBattlefieldAndReturn(player1, new HumblerOfMortals());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, humbler, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, humbler, Keyword.TRAMPLE)).isFalse();
    }

    private void castHumblerOfMortals() {
        harness.setHand(player1, List.of(new HumblerOfMortals()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }

    private Permanent findHumbler() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof HumblerOfMortals)
                .findFirst()
                .orElseThrow();
    }
}
