package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LifeningElemental.class, Shock.class, GrizzlyBears.class, LavaAxe.class, Twincast.class})
class LifeningElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Splices lifelink onto an instant and stays in hand")
    void splicesLifelinkOntoInstant() {
        Card shock = new Shock();
        LifeningElemental elemental = new LifeningElemental();
        harness.setHand(player1, List.of(shock, elemental));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elemental);
    }

    @Test
    @DisplayName("Cannot splice onto a permanent spell")
    void rejectsPermanentHost() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new LifeningElemental()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, null, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only be used when casting an instant or sorcery");
    }

    @Test
    @DisplayName("Splices lifelink onto a sorcery when the splice card precedes the host in hand")
    void splicesOntoSorceryWithEarlierHandIndex() {
        LifeningElemental elemental = new LifeningElemental();
        harness.setHand(player1, List.of(elemental, new LavaAxe()));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castWithSplice(player1, 1, player2.getId(), List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 15);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elemental);
    }

    @Test
    @DisplayName("Multiple spliced copies grant lifelink without multiplying life gained")
    void multipleCopiesDoNotMultiplyLifelink() {
        LifeningElemental first = new LifeningElemental();
        LifeningElemental second = new LifeningElemental();
        harness.setHand(player1, List.of(new Shock(), first, second));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1, 2));
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Keeping Lifening Elemental in hand does not grant lifelink without splicing")
    void unsplicedSpellDoesNotGainLifelink() {
        LifeningElemental elemental = new LifeningElemental();
        harness.setHand(player1, List.of(new Shock(), elemental));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elemental);
    }

    @Test
    @DisplayName("A spell copy retains spliced lifelink and gains life for its own controller")
    void copyRetainsSplicedLifelinkForCopyController() {
        Shock shock = new Shock();
        LifeningElemental elemental = new LifeningElemental();
        harness.setHand(player1, List.of(shock, elemental));
        harness.setHand(player2, List.of(new Twincast()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 8);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elemental);
    }
}
