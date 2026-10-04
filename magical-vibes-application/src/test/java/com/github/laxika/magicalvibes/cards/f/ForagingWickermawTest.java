package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({ForagingWickermaw.class})
class ForagingWickermawTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils 1")
    void entersWithSurveil() {
        Card topCard = new ForagingWickermaw();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new ForagingWickermaw()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("The mana ability adds the chosen color and makes the creature that color")
    void manaAbilityAddsManaAndChangesSourceColor() {
        Permanent wickermaw = addCreatureReady(player1, new ForagingWickermaw());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, wickermaw)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("The mana ability can be activated only once each turn")
    void manaAbilityIsLimitedToOnceEachTurn() {
        addCreatureReady(player1, new ForagingWickermaw());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The color change wears off at end of turn")
    void colorChangeWearsOffAtEndOfTurn() {
        Permanent wickermaw = addCreatureReady(player1, new ForagingWickermaw());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        assertThat(gqs.getEffectiveColors(gd, wickermaw)).containsExactly(CardColor.GREEN);

        wickermaw.resetModifiers();
        gd.expireEndOfTurnFloatingEffects();

        assertThat(gqs.getEffectiveColors(gd, wickermaw)).isEmpty();
    }

    @Test
    void surveilCanLeaveTheTopCardInTheLibrary() {
        Card topCard = new ForagingWickermaw();
        Card secondCard = new ForagingWickermaw();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new ForagingWickermaw()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void surveilWithAnEmptyLibraryCompletesWithoutAChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ForagingWickermaw()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void manaAbilityWorksWhileTappedAndSummoningSickWithoutUsingTheStack() {
        Permanent wickermaw = harness.addToBattlefieldAndReturn(player1, new ForagingWickermaw());
        wickermaw.setSummoningSick(true);
        wickermaw.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, wickermaw)).containsExactly(CardColor.WHITE);
        assertThat(wickermaw.isTapped()).isTrue();
    }

    @Test
    void manaAbilityCanBeUsedAgainOnTheOpponentsTurnAndPreviousColorExpires() {
        harness.forceActivePlayer(player1);
        Permanent wickermaw = addCreatureReady(player1, new ForagingWickermaw());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectiveColors(gd, wickermaw)).isEmpty();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, wickermaw)).containsExactly(CardColor.BLACK);
    }
}
