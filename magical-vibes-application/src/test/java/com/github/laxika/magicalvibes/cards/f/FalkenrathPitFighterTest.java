package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FalkenrathPitFighter.class, GrizzlyBears.class, Island.class, Shock.class})
class FalkenrathPitFighterTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate before an opponent loses life this turn")
    void cannotActivateBeforeOpponentLosesLife() {
        addReadyPitFighter();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        forceMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent lost life this turn");
    }

    @Test
    @DisplayName("After an opponent loses life, discarding and sacrificing a Vampire draws two cards")
    void drawsTwoCardsAfterPayingCosts() {
        addReadyPitFighter();
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new Shock(), new GrizzlyBears()));

        harness.addMana(player1, ManaColor.RED, 1);
        forceMainPhase(player1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        forceMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Falkenrath Pit Fighter");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Island", "Island");
    }

    private Permanent addReadyPitFighter() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new FalkenrathPitFighter());
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    void losingLifeYourselfDoesNotAllowActivation() {
        addReadyPitFighter();
        dealShockTo(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent lost life this turn");
    }

    @Test
    void cannotActivateWithoutACardToDiscard() {
        addReadyPitFighter();
        dealShockTo(player2);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Falkenrath Pit Fighter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateWhileTappedAndSummoningSickOnOpponentsTurn() {
        Permanent fighter = harness.addToBattlefieldAndReturn(player1, new FalkenrathPitFighter());
        fighter.setSummoningSick(true);
        fighter.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        dealShockTo(player2);
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Falkenrath Pit Fighter");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void canSacrificeAnotherVampireAndKeepTheSource() {
        addReadyPitFighter();
        Permanent other = harness.addToBattlefieldAndReturn(player1, new FalkenrathPitFighter());
        dealShockTo(player2);
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, other.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getId()).isNotEqualTo(other.getId());
        harness.assertInGraveyard(player1, "Falkenrath Pit Fighter");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @CardUsed({ArtificialEvolution.class, Bitterblossom.class})
    void canSacrificeANoncreatureKindredVampire() {
        Permanent fighter = addReadyPitFighter();
        Permanent blossom = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, blossom.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "VAMPIRE");
        assertThat(gqs.hasEffectiveSubtype(gd, blossom, CardSubtype.VAMPIRE)).isTrue();

        dealShockTo(player2);
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, blossom.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bitterblossom");
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(fighter.getId()).doesNotContain(blossom.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private void dealShockTo(Player player) {
        harness.setHand(player1, List.of(new Shock(), new Island()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player.getId());
        harness.passBothPriorities();
    }

    private void forceMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
