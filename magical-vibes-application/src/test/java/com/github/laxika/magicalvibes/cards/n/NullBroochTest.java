package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NullBroochTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a noncreature spell and discards its controller's hand")
    void countersNoncreatureSpellAndDiscardsHand() {
        Permanent brooch = harness.addToBattlefieldAndReturn(player1, new NullBrooch());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(might));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, target.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, might.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Might of Oaks");
        assertThat(brooch.isTapped()).isTrue();
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        Permanent brooch = harness.addToBattlefieldAndReturn(player1, new NullBrooch());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(brooch.isTapped()).isFalse();
    }
}
