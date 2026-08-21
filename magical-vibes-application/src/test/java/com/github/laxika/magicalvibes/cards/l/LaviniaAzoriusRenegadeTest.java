package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AsForetold;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LaviniaAzoriusRenegadeTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent can't cast a noncreature spell with mana value greater than their land count")
    void restrictsNoncreatureSpellByOpponentsLandCount() {
        harness.addToBattlefield(player1, new LaviniaAzoriusRenegade());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opponent can cast a noncreature spell at or below their land count")
    void allowsNoncreatureSpellAtLandCount() {
        harness.addToBattlefield(player1, new LaviniaAzoriusRenegade());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new Plains());
        }
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }

    @Test
    @DisplayName("Counters an opponent's spell cast without spending mana")
    void countersOpponentFreeCast() {
        harness.addToBattlefield(player1, new LaviniaAzoriusRenegade());
        var asForetold = harness.addToBattlefieldAndReturn(player2, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 2);
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not counter an opponent's spell cast with mana")
    void doesNotCounterPaidSpell() {
        harness.addToBattlefield(player1, new LaviniaAzoriusRenegade());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
