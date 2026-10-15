package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({RestlessReef.class})
class RestlessReefTest extends BaseCardTest {

    @Test
    @DisplayName("Restless Reef enters tapped and adds blue or black mana")
    void entersTappedAndAddsChosenMana() {
        harness.setHand(player1, List.of(new RestlessReef()));
        harness.playLand(player1, 0);

        Permanent reef = findPermanent(player1, "Restless Reef");
        assertThat(reef.isTapped()).isTrue();

        reef.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Restless Reef becomes a 4/4 blue and black Shark with deathtouch")
    void animatesIntoShark() {
        Permanent reef = addReadyReef(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, reef)).isTrue();
        assertThat(gqs.isLand(gd, reef)).isTrue();
        assertThat(gqs.getEffectivePower(gd, reef)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, reef)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, reef))
                .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.BLACK);
        assertThat(reef.getTransientSubtypes()).containsExactly(CardSubtype.SHARK);
        assertThat(gqs.hasKeyword(gd, reef, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Restless Reef's animation ends at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent reef = addReadyReef(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, reef)).isFalse();
        assertThat(gqs.isLand(gd, reef)).isTrue();
        assertThat(gqs.hasKeyword(gd, reef, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Attacking lets its controller choose a player to mill four cards")
    void attackingMillsChosenPlayer() {
        Permanent reef = addReadyReef(player1);
        harness.setLibrary(player2, libraryWithFiveCards());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(reef)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Restless Reef can add black mana without using the stack")
    void addsBlackMana() {
        Permanent reef = addReadyReef(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(reef.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Restless Reef can target its own controller with its attack trigger")
    void attackingCanMillItsController() {
        addReadyReef(player1);
        harness.setLibrary(player1, libraryWithFiveCards());
        harness.setLibrary(player2, libraryWithFiveCards());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Restless Reef mills all remaining cards from a library with fewer than four")
    void attackingMillsShortLibrary() {
        addReadyReef(player1);
        List<Card> library = List.of(new RestlessReef(), new RestlessReef());
        harness.setLibrary(player2, library);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(library);
    }

    private Permanent addReadyReef(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new RestlessReef());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private List<Card> libraryWithFiveCards() {
        return List.of(
                new RestlessReef(),
                new RestlessReef(),
                new RestlessReef(),
                new RestlessReef(),
                new RestlessReef()
        );
    }
}
