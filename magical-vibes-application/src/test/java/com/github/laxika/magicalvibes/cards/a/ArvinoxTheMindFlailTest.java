package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SowerOfTemptation;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArvinoxTheMindFlail.class, SowerOfTemptation.class, GrizzlyBears.class, Forest.class})
class ArvinoxTheMindFlailTest extends BaseCardTest {

    @Test
    @DisplayName("Isn't a creature until its controller controls three permanents they do not own")
    void becomesCreatureWithThreePermanentsNotOwned() {
        Permanent arvinox = harness.addToBattlefieldAndReturn(player1, new ArvinoxTheMindFlail());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SowerOfTemptation(), new SowerOfTemptation(),
                new SowerOfTemptation()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThat(gqs.isCreature(gd, arvinox)).isFalse();
        assertThat(gqs.isEnchantment(gd, arvinox)).isTrue();

        castAndResolveSower(first.getId());
        castAndResolveSower(second.getId());
        assertThat(gqs.isCreature(gd, arvinox)).isFalse();

        castAndResolveSower(third.getId());
        assertThat(gqs.isCreature(gd, arvinox)).isTrue();
    }

    @Test
    @DisplayName("Exiles the bottom card of each opponent's library face down at your end step")
    void exilesBottomCardOfEachOpponentsLibrary() {
        Permanent arvinox = harness.addToBattlefieldAndReturn(player1, new ArvinoxTheMindFlail());
        Card ownBottom = new Forest();
        Card opponentTop = new Forest();
        Card opponentBottom = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownBottom));
        harness.setLibrary(player2, List.of(opponentTop, opponentBottom));

        resolveEndStep(player1);

        assertThat(gd.getCardsExiledByPermanent(arvinox.getId())).containsExactly(opponentBottom);
        assertThat(gd.exiledCards).filteredOn(entry -> arvinox.getId().equals(entry.sourcePermanentId()))
                .allMatch(ExiledCardEntry::faceDown);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownBottom);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
    }

    @Test
    @DisplayName("May cast an exiled permanent with mana of any color")
    void mayCastExiledPermanentWithAnyColorMana() {
        Permanent arvinox = harness.addToBattlefieldAndReturn(player1, new ArvinoxTheMindFlail());
        Card exiled = new GrizzlyBears();
        harness.setLibrary(player2, List.of(new Forest(), exiled));

        resolveEndStep(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(exiled.getId()));
        assertThat(gd.getCardsExiledByPermanent(arvinox.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not grant permission to cast exiled lands")
    void doesNotAllowCastingExiledLand() {
        harness.addToBattlefieldAndReturn(player1, new ArvinoxTheMindFlail());
        Card exiledLand = new Forest();
        harness.setLibrary(player2, List.of(new GrizzlyBears(), exiledLand));

        resolveEndStep(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, exiledLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The permission lasts while the exiled card remains exiled")
    void permissionRemainsAfterArvinoxLeaves() {
        Permanent arvinox = harness.addToBattlefieldAndReturn(player1, new ArvinoxTheMindFlail());
        Card exiled = new GrizzlyBears();
        harness.setLibrary(player2, List.of(new Forest(), exiled));

        resolveEndStep(player1);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, arvinox);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(exiled.getId()));
    }

    private void castAndResolveSower(UUID targetId) {
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void resolveEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
