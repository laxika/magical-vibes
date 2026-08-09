package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BubblingMuckTest extends BaseCardTest {

    @Test
    @DisplayName("A Swamp tapped for mana adds an additional {B}")
    void addsBlackManaWhenSwampIsTapped() {
        harness.setHand(player1, List.of(new BubblingMuck()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addToBattlefield(player1, new Swamp());

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("The effect applies to an opponent's Swamp and not to a Mountain")
    void appliesSymmetricallyOnlyToSwamps() {
        harness.setHand(player1, List.of(new BubblingMuck()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Swamp());

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
        harness.tapPermanent(player1, 0);
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("The additional mana effect expires at the end of the turn")
    void expiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new BubblingMuck()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addToBattlefield(player1, new Swamp());

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent swamp = findPermanent(player1, "Swamp");
        swamp.untap();
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }
}
