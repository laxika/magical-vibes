package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.ChandraHopesBeacon;
import com.github.laxika.magicalvibes.cards.i.InvasionOfRegatha;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SiegeMastodon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NahirisWarcrafting.class, HillGiant.class, SiegeMastodon.class, Forest.class, Shock.class,
        ChandraHopesBeacon.class, InvasionOfRegatha.class})
class NahirisWarcraftingTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles one of the top cards equal to the excess damage and bottoms the rest")
    void exilesOneCardAndBottomsTheRest() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Card forest1 = new Forest();
        Card shock = new Shock();
        Card forest2 = new Forest();
        setLibrary(List.of(forest1, shock, forest2));
        prepareSpell();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest1, shock);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shock);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest1, forest2);
        assertThat(gd.exilePlayPermissions).containsEntry(shock.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(shock.getId());
    }

    @Test
    @DisplayName("Does not look at cards when the damage is not excess")
    void noExcessDamageDoesNotLookAtCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SiegeMastodon());
        Card forest = new Forest();
        Card shock = new Shock();
        setLibrary(List.of(forest, shock));
        prepareSpell();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("May decline to exile a looked-at card")
    void mayDecline() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Card forest = new Forest();
        Card shock = new Shock();
        setLibrary(List.of(forest, shock));
        prepareSpell();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, shock);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(forest.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(shock.getId());
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        setLibrary(List.of(new Forest()));
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dealsFiveDamageToABattleWithoutExcess() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InvasionOfRegatha());
        target.setCounterCount(CounterType.DEFENSE, 7);
        Card forest = new Forest();
        setLibrary(List.of(forest));
        prepareSpell();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void dealsFiveDamageToAPlaneswalkerWithoutExcess() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraHopesBeacon());
        target.setCounterCount(CounterType.LOYALTY, 7);
        Card forest = new Forest();
        setLibrary(List.of(forest));
        prepareSpell();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void priorMarkedDamageIncreasesExcess() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SiegeMastodon());
        target.setMarkedDamage(2);
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        setLibrary(List.of(first, second, third));
        prepareSpell();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first);
    }

    @Test
    void excessUsesPlaneswalkersCurrentLoyalty() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraHopesBeacon());
        target.setCounterCount(CounterType.LOYALTY, 3);
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        setLibrary(List.of(first, second, third));
        prepareSpell();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second);
    }

    @Test
    void excessUsesBattlesCurrentDefense() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InvasionOfRegatha());
        target.setCounterCount(CounterType.DEFENSE, 3);
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        setLibrary(List.of(first, second, third));
        prepareSpell();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first);
    }

    @Test
    void looksAtOnlyAvailableCardsWhenLibraryIsShort() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Card forest = new Forest();
        setLibrary(List.of(forest));
        prepareSpell();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(forest);
    }

    @Test
    void emptyLibraryDoesNotPromptOrCauseADrawLoss() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        setLibrary(List.of());
        prepareSpell();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void illegalTargetOnResolutionDoesNotLookAtLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Card forest = new Forest();
        setLibrary(List.of(forest));
        prepareSpell();

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void exiledSpellRequiresItsNormalManaCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Card shock = new Shock();
        setLibrary(List.of(shock));
        prepareSpell();
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void exiledLandCanBePlayedThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Card forest = new Forest();
        setLibrary(List.of(forest));
        prepareSpell();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.castFromExile(player1, forest.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void exiledLandDoesNotGrantAnAdditionalLandPlay() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Card forest = new Forest();
        setLibrary(List.of(forest));
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        prepareSpell();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThatThrownBy(() -> harness.castFromExile(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(forest);
    }

    @Test
    void permissionExpiresAtEndOfThisTurnAndCardRemainsExiled() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Card shock = new Shock();
        setLibrary(List.of(shock));
        prepareSpell();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shock);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(shock.getId());
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new NahirisWarcrafting()));
        harness.addMana(player1, ManaColor.RED, 3);
    }

    private void setLibrary(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
