package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InterdimensionalWebWatch.class, Divination.class, Forest.class})
class InterdimensionalWebWatchTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top two cards and lets you play them until the end of your next turn")
    void exilesTopTwoCardsWithPlayPermission() {
        Card first = new Divination();
        Card second = new Forest();
        castWatchWithLibrary(first, second);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsAwaitNextTurnOfPlayer)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
    }

    @Test
    @DisplayName("Adds two mana with a separate color choice for each mana")
    void addsTwoExileSpellOnlyMana() {
        Permanent watch = harness.addToBattlefieldAndReturn(player1, new InterdimensionalWebWatch());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "RED");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getTotal()).isZero();
        assertThat(pool.getExiledSpellOnlyMana(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.getExiledSpellOnlyMana(ManaColor.RED)).isEqualTo(1);
        assertThat(watch.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exile-only mana pays for a spell cast from exile")
    void exileOnlyManaPaysForExiledSpell() {
        Card first = new Divination();
        castWatchWithLibrary(first, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, first.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(first);
        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getExiledSpellOnlyManaTotal()).isZero();
    }

    @Test
    @DisplayName("Exile-only mana cannot pay for a spell cast from hand")
    void exileOnlyManaCannotPayForHandSpell() {
        harness.addToBattlefield(player1, new InterdimensionalWebWatch());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new InterdimensionalWebWatch()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getExiledSpellOnlyMana(ManaColor.BLUE))
                .isEqualTo(2);
    }

    @Test
    void canPlayExiledLandButMustObserveLandPlayLimit() {
        Card first = new Forest();
        Card second = new Forest();
        castWatchWithLibrary(first, second);

        harness.castFromExile(player1, first.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void permissionLastsThroughNextTurnAndThenExpires() {
        Card first = new Forest();
        Card second = new Forest();
        castWatchWithLibrary(first, second);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, first.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, first.getId());
        harness.assertOnBattlefield(player1, "Forest");

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    void exilesOnlyAvailableCardFromShortLibrary() {
        Card onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.enterBattlefieldAndReturn(player1, new InterdimensionalWebWatch());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.exilePlayPermissions).containsEntry(onlyCard.getId(), player1.getId());
    }

    @Test
    void emptyLibraryDoesNotPreventManaAbility() {
        harness.setLibrary(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new InterdimensionalWebWatch());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "GREEN");
        assertThat(gd.playerManaPools.get(player1.getId()).getExiledSpellOnlyMana(ManaColor.GREEN))
                .isEqualTo(2);
    }

    @Test
    void permissionDoesNotWaiveManaCostOrAllowOpponentToPlayCards() {
        Card spell = new InterdimensionalWebWatch();
        castWatchWithLibrary(spell, new Forest());

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        assertThatThrownBy(() -> harness.castFromExile(player2, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    private void castWatchWithLibrary(Card first, Card second) {
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new InterdimensionalWebWatch()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
