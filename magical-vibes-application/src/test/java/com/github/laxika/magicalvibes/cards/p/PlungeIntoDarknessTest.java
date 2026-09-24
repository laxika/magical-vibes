package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AuriokChampion;
import com.github.laxika.magicalvibes.cards.k.KrarkClanIronworks;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlungeIntoDarkness.class, AuriokChampion.class, KrarkClanIronworks.class})
class PlungeIntoDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice mode gains three life for each sacrificed creature")
    void sacrificeModeGainsLifePerCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AuriokChampion());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AuriokChampion());
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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AuriokChampion());
        cast(0);
        harness.passBothPriorities();

        int lifeBefore = gd.getLife(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrifice mode does not sacrifice noncreature permanents")
    void sacrificeModeOnlyAllowsCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AuriokChampion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new KrarkClanIronworks());
        cast(0);
        harness.passBothPriorities();

        int lifeBefore = gd.getLife(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Life-payment mode looks at X cards and exiles the rest")
    void lifePaymentModeLooksAtPaidAmount() {
        Card first = new AuriokChampion();
        Card chosen = new KrarkClanIronworks();
        Card third = new AuriokChampion();
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
    @DisplayName("Life-payment mode permits paying zero life without looking at cards")
    void lifePaymentModeAllowsPayingZero() {
        Card first = new AuriokChampion();
        Card second = new KrarkClanIronworks();
        harness.setLibrary(player1, List.of(first, second));
        cast(1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing both modes pays the entwine mana and resolves both modes")
    void bothModesResolveWithEntwineMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AuriokChampion());
        Card chosen = new AuriokChampion();
        Card exiled = new KrarkClanIronworks();
        harness.setLibrary(player1, List.of(chosen, exiled));
        addMana(3);
        harness.setHand(player1, List.of(new PlungeIntoDarkness()));
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1}, List.of());
        harness.passBothPriorities();

        int lifeBefore = gd.getLife(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3 - 2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
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
