package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FearlessPup;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreatHallOfStarnheim.class, FearlessPup.class})
class GreatHallOfStarnheimTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and taps for black mana")
    void entersTappedAndTapsForBlack() {
        harness.setHand(player1, List.of(new GreatHallOfStarnheim()));
        harness.playLand(player1, 0);
        Permanent land = findPermanent(player1, "Great Hall of Starnheim");

        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrifices itself and a creature to create a 4/4 flying vigilant Angel Warrior")
    void createsAngelWarriorAfterPayingCosts() {
        harness.addToBattlefield(player1, new GreatHallOfStarnheim());
        addCreatureReady(player1, new FearlessPup());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Great Hall of Starnheim");
        harness.assertInGraveyard(player1, "Fearless Pup");

        Permanent token = findPermanent(player1, "Angel Warrior");
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ANGEL, CardSubtype.WARRIOR);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING, Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Cannot activate the token ability without a creature to sacrifice")
    void requiresCreatureToSacrifice() {
        harness.addToBattlefield(player1, new GreatHallOfStarnheim());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new GreatHallOfStarnheim());
        harness.addToBattlefield(player2, new FearlessPup());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Great Hall of Starnheim");
        harness.assertOnBattlefield(player2, "Fearless Pup");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The land must be untapped to pay the tap cost")
    void cannotActivateTappedLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new GreatHallOfStarnheim());
        harness.addToBattlefield(player1, new FearlessPup());
        land.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Great Hall of Starnheim");
        harness.assertOnBattlefield(player1, "Fearless Pup");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activation requires two white mana, not generic mana")
    void requiresCorrectColoredMana() {
        harness.addToBattlefield(player1, new GreatHallOfStarnheim());
        harness.addToBattlefield(player1, new FearlessPup());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Great Hall of Starnheim");
        harness.assertOnBattlefield(player1, "Fearless Pup");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The token ability cannot be activated outside a main phase")
    void cannotActivateDuringCombat() {
        harness.addToBattlefield(player1, new GreatHallOfStarnheim());
        harness.addToBattlefield(player1, new FearlessPup());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Great Hall of Starnheim");
        harness.assertOnBattlefield(player1, "Fearless Pup");
    }

    @Test
    @DisplayName("The token ability cannot be activated during the opponent's turn")
    void cannotActivateDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new GreatHallOfStarnheim());
        harness.addToBattlefield(player1, new FearlessPup());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Great Hall of Starnheim");
        harness.assertOnBattlefield(player1, "Fearless Pup");
    }

    @Test
    @DisplayName("Postcombat activation sacrifices a tapped new creature before the token resolves")
    void paysCostsBeforeResolutionInPostcombatMain() {
        harness.addToBattlefield(player1, new GreatHallOfStarnheim());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FearlessPup());
        creature.tap();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "Great Hall of Starnheim");
        harness.assertInGraveyard(player1, "Fearless Pup");
        harness.assertNotOnBattlefield(player1, "Angel Warrior");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Angel Warrior")).hasSize(1);
        assertThat(findPermanent(player1, "Angel Warrior").isTapped()).isFalse();
        harness.assertNotOnBattlefield(player2, "Angel Warrior");
    }

    @Test
    @DisplayName("The token ability cannot be activated while another ability is on the stack")
    void requiresEmptyStack() {
        harness.addToBattlefield(player1, new GreatHallOfStarnheim());
        harness.addToBattlefield(player1, new GreatHallOfStarnheim());
        harness.addToBattlefield(player1, new FearlessPup());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player1, new FearlessPup());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Great Hall of Starnheim");
        harness.assertOnBattlefield(player1, "Fearless Pup");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Angel Warrior")).hasSize(1);
    }

    @Test
    @DisplayName("The controller chooses which creature to sacrifice when several are available")
    void choosesCreatureToSacrifice() {
        harness.addToBattlefield(player1, new GreatHallOfStarnheim());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new FearlessPup());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new FearlessPup());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        harness.assertInGraveyard(player1, "Great Hall of Starnheim");
        harness.assertInGraveyard(player1, "Fearless Pup");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(survivor).doesNotContain(sacrifice);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Fearless Pup")).containsExactly(survivor);
        assertThat(findPermanents(player1, "Angel Warrior")).hasSize(1);
    }
}
