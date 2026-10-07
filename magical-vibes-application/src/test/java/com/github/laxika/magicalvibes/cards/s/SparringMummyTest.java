package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.ThoseWhoServe;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SparringMummy.class, ThoseWhoServe.class, Island.class})
class SparringMummyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB untaps the target creature")
    void etbUntapsTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new ThoseWhoServe());
        bears.tap();
        assertThat(bears.isTapped()).isTrue();
        UUID targetId = bears.getId();

        harness.setHand(player1, List.of(new SparringMummy()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(bears.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Sparring Mummy");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Island()).getId();

        harness.setHand(player1, List.of(new SparringMummy()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void canTargetItselfWhenItIsTheOnlyCreature() {
        harness.setHand(player1, List.of(new SparringMummy()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent mummy = findPermanent(player1, "Sparring Mummy");
        harness.handlePermanentChosen(player1, mummy.getId());
        mummy.tap();
        harness.passBothPriorities();

        assertThat(mummy.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetAnUntappedCreatureAndUntapsOnlyThatCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        other.tap();
        harness.setHand(player1, List.of(new SparringMummy()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(target.isTapped()).isFalse();
        target.tap();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotUntapAnotherCreatureWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ThoseWhoServe());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new ThoseWhoServe());
        target.tap();
        other.tap();
        harness.setHand(player1, List.of(new SparringMummy()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(other.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Sparring Mummy");
        assertThat(gd.stack).isEmpty();
    }
}
