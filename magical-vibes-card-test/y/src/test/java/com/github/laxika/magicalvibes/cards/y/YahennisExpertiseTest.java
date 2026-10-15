package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarWastes;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YahennisExpertise.class, GrizzlyBears.class, SerraAngel.class,
        TrainedArmodon.class, LlanowarWastes.class})
class YahennisExpertiseTest extends BaseCardTest {

    @Test
    @DisplayName("Gives all creatures -3/-3 until end of turn")
    void weakensAllCreaturesUntilEndOfTurn() {
        Permanent ownAngel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent opposingAngel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castExpertise(List.of(new YahennisExpertise()));

        assertThat(ownAngel.getEffectivePower()).isEqualTo(1);
        assertThat(ownAngel.getEffectiveToughness()).isEqualTo(1);
        assertThat(opposingAngel.getEffectivePower()).isEqualTo(1);
        assertThat(opposingAngel.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownAngel.getEffectivePower()).isEqualTo(4);
        assertThat(ownAngel.getEffectiveToughness()).isEqualTo(4);
        assertThat(opposingAngel.getEffectivePower()).isEqualTo(4);
        assertThat(opposingAngel.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Offers a spell with mana value three or less from hand for free")
    void castsLowManaValueSpellFromHand() {
        GrizzlyBears bears = new GrizzlyBears();
        castExpertise(List.of(new YahennisExpertise(), bears));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Does not offer a spell with mana value greater than three")
    void doesNotOfferHighManaValueSpell() {
        castExpertise(List.of(new YahennisExpertise(), new SerraAngel()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creatures with zero or less toughness die on both sides")
    void killsSmallCreaturesOnBothSides() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new TrainedArmodon());

        castExpertise(List.of(new YahennisExpertise()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Trained Armodon");
        harness.assertInGraveyard(player2, "Trained Armodon");
    }

    @Test
    @DisplayName("Can decline the free spell while the creature reduction still applies")
    void canDeclineFreeSpell() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        castExpertise(List.of(new YahennisExpertise(), new GrizzlyBears()));

        harness.handleMayAbilityChosen(player1, false);

        assertThat(angel.getEffectiveToughness()).isEqualTo(1);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Yahenni's Expertise");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can cast a spell with mana value exactly three with no green mana")
    void castsManaValueThreeSpell() {
        castExpertise(List.of(new YahennisExpertise(), new TrainedArmodon()));

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Trained Armodon");
        harness.assertNotInHand(player1, "Trained Armodon");
        assertThat(findPermanent(player1, "Trained Armodon").getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Accepting one free spell removes the other offers")
    void castsOnlyOneSpell() {
        castExpertise(List.of(new YahennisExpertise(), new GrizzlyBears(), new TrainedArmodon()));

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Trained Armodon");
        harness.assertNotOnBattlefield(player1, "Trained Armodon");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the first offer allows a different eligible spell")
    void canChooseLaterSpell() {
        castExpertise(List.of(new YahennisExpertise(), new GrizzlyBears(), new TrainedArmodon()));

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Trained Armodon");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Lands are not offered as free spells")
    void doesNotOfferLand() {
        castExpertise(List.of(new YahennisExpertise(), new LlanowarWastes()));

        harness.assertInHand(player1, "Llanowar Wastes");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dying creatures remain until the free spell is cast, then die before it resolves")
    void waitsForFreeCastBeforeCheckingZeroToughness() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castExpertise(List.of(new YahennisExpertise(), new TrainedArmodon()));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Trained Armodon");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Trained Armodon");
        assertThat(findPermanent(player1, "Trained Armodon").getEffectiveToughness()).isEqualTo(3);
    }

    private void castExpertise(List<Card> hand) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, List.of());
    }
}
