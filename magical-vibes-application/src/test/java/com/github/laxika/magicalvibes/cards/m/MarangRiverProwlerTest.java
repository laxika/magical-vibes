package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SultaiEmissary;
import com.github.laxika.magicalvibes.cards.f.FrontierMastodon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarangRiverProwler.class, SultaiEmissary.class, FrontierMastodon.class})
class MarangRiverProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast from graveyard while controlling a black permanent")
    void canCastFromGraveyardWithBlackPermanent() {
        harness.setGraveyard(player1, List.of(new MarangRiverProwler()));
        harness.addToBattlefield(player1, new SultaiEmissary());
        addManaToCast();

        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Can cast from graveyard while controlling a green permanent")
    void canCastFromGraveyardWithGreenPermanent() {
        harness.setGraveyard(player1, List.of(new MarangRiverProwler()));
        harness.addToBattlefield(player1, new FrontierMastodon());
        addManaToCast();

        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot cast from graveyard without a black or green permanent")
    void cannotCastFromGraveyardWithoutBlackOrGreenPermanent() {
        harness.setGraveyard(player1, List.of(new MarangRiverProwler()));
        addManaToCast();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Opponent's black permanent does not enable casting from graveyard")
    void opponentBlackPermanentDoesNotEnableGraveyardCast() {
        harness.setGraveyard(player1, List.of(new MarangRiverProwler()));
        harness.addToBattlefield(player2, new SultaiEmissary());
        addManaToCast();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Cannot be blocked")
    void cannotBeBlocked() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new FrontierMastodon());
        blocker.setSummoningSick(false);

        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new MarangRiverProwler());
        prowler.setSummoningSick(false);
        prowler.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Cannot be declared as a blocker")
    void cannotBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new FrontierMastodon());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent prowler = harness.addToBattlefieldAndReturn(player2, new MarangRiverProwler());
        prowler.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A blue permanent does not enable casting from the graveyard")
    void bluePermanentDoesNotEnableGraveyardCast() {
        harness.setGraveyard(player1, List.of(new MarangRiverProwler()));
        harness.addToBattlefield(player1, new MarangRiverProwler());
        addManaToCast();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Graveyard permission does not allow casting during combat")
    void cannotCastFromGraveyardDuringCombat() {
        harness.setGraveyard(player1, List.of(new MarangRiverProwler()));
        harness.addToBattlefield(player1, new FrontierMastodon());
        addManaToCast();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cast sorcery-speed spell from graveyard now");
    }

    @Test
    @DisplayName("Graveyard casting still requires the normal mana cost")
    void cannotCastFromGraveyardWithoutEnoughMana() {
        harness.setGraveyard(player1, List.of(new MarangRiverProwler()));
        harness.addToBattlefield(player1, new FrontierMastodon());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Marang River Prowler");
    }

    @Test
    @DisplayName("Losing the qualifying permanent after casting does not stop resolution")
    void resolvesAfterQualifyingPermanentLeaves() {
        harness.setGraveyard(player1, List.of(new MarangRiverProwler()));
        harness.addToBattlefield(player1, new FrontierMastodon());
        addManaToCast();
        harness.castFromGraveyard(player1, 0);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Marang River Prowler");
        harness.assertNotInGraveyard(player1, "Marang River Prowler");
        assertThat(gd.stack).isEmpty();
    }

    private void addManaToCast() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
