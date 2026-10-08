package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TerrapactIntimidator.class, Forest.class})
class TerrapactIntimidatorTest extends BaseCardTest {

    @Test
    void targetOpponentMayHaveControllerCreateTwoLanders() {
        castIntimidator();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        assertThat(findPermanents(player1, "Lander")).hasSize(2);
        assertThat(findPermanents(player2, "Lander")).isEmpty();
        assertThat(findPermanent(player1, "Terrapact Intimidator")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void decliningPutsTwoPlusOnePlusOneCountersOnThisCreature() {
        castIntimidator();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(findPermanent(player1, "Terrapact Intimidator")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotTargetController() {
        harness.setHand(player1, List.of(new TerrapactIntimidator()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void landerCanSearchForBasicLandTappedOnTheTurnItEnters() {
        castIntimidator();
        harness.handleMayAbilityChosen(player2, true);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(new TerrapactIntimidator(), forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateLander();

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(findPermanent(player1, "Forest").getCard()).isSameAs(forest);
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(findPermanents(player2, "Forest")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1)
                .allMatch(card -> card instanceof TerrapactIntimidator);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void landerSearchCanFailToFindAnAvailableBasicLand() {
        castIntimidator();
        harness.handleMayAbilityChosen(player2, true);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateLander();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void landerCanBeSacrificedWithNoBasicLandInLibrary() {
        castIntimidator();
        harness.handleMayAbilityChosen(player2, true);
        harness.setLibrary(player1, List.of(new TerrapactIntimidator()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateLander();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void landerCannotActivateWithoutTwoMana() {
        castIntimidator();
        harness.handleMayAbilityChosen(player2, true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(this::activateLander).isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Lander")).hasSize(2)
                .allMatch(lander -> !lander.isTapped());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedLanderCannotActivate() {
        castIntimidator();
        harness.handleMayAbilityChosen(player2, true);
        findPermanent(player1, "Lander").tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(this::activateLander).isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Lander")).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controllerCannotMakeTheOpponentsChoice() {
        castIntimidator();

        assertThatThrownBy(() -> harness.handleMayAbilityChosen(player1, true))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);
        assertThat(findPermanent(player1, "Terrapact Intimidator")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void activateLander() {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Lander")), null, null);
    }

    private void castIntimidator() {
        harness.setHand(player1, List.of(new TerrapactIntimidator()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
