package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TradingPost.class, AngelsFeather.class, GrizzlyBears.class, Shock.class, Spellbook.class})
class TradingPostTest extends BaseCardTest {

    private void setUpMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Discarding a card gains 4 life")
    void discardGainsFourLife() {
        setUpMain();
        harness.addToBattlefield(player1, new TradingPost());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 4);
    }

    @Test
    @DisplayName("Cannot activate the life-gain ability with an empty hand")
    void cannotGainLifeWithoutCardToDiscard() {
        setUpMain();
        harness.addToBattlefield(player1, new TradingPost());
        harness.setHand(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paying 1 life creates a 0/1 Goat token")
    void payLifeCreatesGoat() {
        setUpMain();
        harness.addToBattlefield(player1, new TradingPost());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife - 1);
        Permanent goat = findPermanent(player1, "Goat");
        assertThat(gqs.getEffectivePower(gd, goat)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, goat)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a creature returns a targeted artifact card from the graveyard to hand")
    void sacrificeCreatureReturnsArtifact() {
        setUpMain();
        harness.addToBattlefield(player1, new TradingPost());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card feather = new AngelsFeather();
        harness.setGraveyard(player1, List.of(feather));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, null, feather.getId(), Zone.GRAVEYARD);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(feather.getId()));
    }

    @Test
    @DisplayName("Cannot target a nonartifact graveyard card with the recursion ability")
    void cannotReturnNonArtifact() {
        setUpMain();
        harness.addToBattlefield(player1, new TradingPost());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 2, null, shock.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing an artifact draws a card")
    void sacrificeArtifactDrawsCard() {
        setUpMain();
        harness.addToBattlefield(player1, new TradingPost());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Spellbook").getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Trading Post can sacrifice itself to draw a card")
    void canSacrificeTradingPostItself() {
        setUpMain();
        harness.addToBattlefield(player1, new TradingPost());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 3, null, null);

        harness.assertNotOnBattlefield(player1, "Trading Post");
        harness.assertInGraveyard(player1, "Trading Post");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Discard is paid before the life gain resolves")
    void discardIsAnActivationCost() {
        setUpMain();
        harness.addToBattlefield(player1, new TradingPost());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Shock");
        harness.assertLife(player1, startingLife);
        assertThat(findPermanent(player1, "Trading Post").isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertLife(player1, startingLife + 4);
    }

    @Test
    @DisplayName("Life is paid before the Goat token is created")
    void lifeIsAnActivationCost() {
        setUpMain();
        harness.addToBattlefield(player1, new TradingPost());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertLife(player1, startingLife - 1);
        harness.assertNotOnBattlefield(player1, "Goat");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Goat");
    }

    @Test
    @DisplayName("A tapped Trading Post cannot activate another ability")
    void cannotActivateTwiceWithoutUntapping() {
        setUpMain();
        harness.addToBattlefield(player1, new TradingPost());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Goat ability requires one mana")
    void cannotActivateWithoutMana() {
        setUpMain();
        harness.addToBattlefield(player1, new TradingPost());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Recursion requires a creature to sacrifice")
    void cannotReturnArtifactWithoutCreature() {
        setUpMain();
        harness.addToBattlefield(player1, new TradingPost());
        Card artifact = new Spellbook();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, null, artifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Recursion cannot target an artifact in an opponent's graveyard")
    void cannotReturnOpponentsArtifact() {
        setUpMain();
        harness.addToBattlefield(player1, new TradingPost());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card artifact = new Spellbook();
        harness.setGraveyard(player2, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, null, artifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
