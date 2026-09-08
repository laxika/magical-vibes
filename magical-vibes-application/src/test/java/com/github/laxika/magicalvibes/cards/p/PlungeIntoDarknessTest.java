package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlungeIntoDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice mode gains three life for each sacrificed creature")
    void sacrificeModeGainsLifePerCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(0);
        harness.passBothPriorities();

        int lifeBefore = gd.getLife(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 6);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrifice mode allows sacrificing no creatures")
    void sacrificeModeCanChooseNone() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(0);
        harness.passBothPriorities();

        int lifeBefore = gd.getLife(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Life-payment mode looks at X cards and exiles the rest")
    void lifePaymentModeLooksAtPaidAmount() {
        Card first = new GrizzlyBears();
        Card chosen = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, chosen, third));
        cast(1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 3);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, third);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing both modes requires the entwine mana")
    void bothModesResolveWithEntwineMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addMana(3);
        harness.setHand(player1, List.of(new PlungeIntoDarkness()));
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1}, List.of());
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
    }

    private void cast(int mode) {
        addMana(2);
        harness.setHand(player1, List.of(new PlungeIntoDarkness()));
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{mode}, List.of());
    }

    private void addMana(int amount) {
        harness.addMana(player1, ManaColor.BLACK, amount);
    }
}
