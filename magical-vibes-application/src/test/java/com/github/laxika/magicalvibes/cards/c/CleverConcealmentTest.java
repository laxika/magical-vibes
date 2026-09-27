package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CleverConcealment.class, GrizzlyBears.class, Forest.class})
class CleverConcealmentTest extends BaseCardTest {

    @Test
    @DisplayName("Phases out any number of nonland permanents you control")
    void phasesOutSelectedPermanents() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CleverConcealment()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(first, second);
    }

    @Test
    @DisplayName("Convoke taps creatures to help cast the spell")
    void castsWithConvoke() {
        Permanent firstConvoker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondConvoker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CleverConcealment()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()),
                List.of(firstConvoker.getId(), secondConvoker.getId()));
        harness.passBothPriorities();

        assertThat(firstConvoker.isTapped()).isTrue();
        assertThat(secondConvoker.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstConvoker, secondConvoker);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new CleverConcealment()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanents you control");
    }
}
