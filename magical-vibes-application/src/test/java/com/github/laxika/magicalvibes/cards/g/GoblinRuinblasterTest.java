package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarWastes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinRuinblasterTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, the ETB ability does not trigger")
    void withoutKickerDoesNotTrigger() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new LlanowarWastes());
        harness.setHand(player1, List.of(new GoblinRuinblaster()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
    }

    @Test
    @DisplayName("When kicked, the ETB ability destroys a target nonbasic land")
    void kickedDestroysNonbasicLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new LlanowarWastes());
        harness.setHand(player1, List.of(new GoblinRuinblaster()));
        addKickedMana();

        harness.castKickedCreature(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(land.getCard());
    }

    @Test
    @DisplayName("Cannot target a basic land")
    void cannotTargetBasicLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new GoblinRuinblaster()));
        addKickedMana();

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonbasic land");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
