package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.t.TravelersAmulet;
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

@CardUsed({HauntedDead.class, TravelersAmulet.class})
class HauntedDeadTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 1/1 white Spirit token with flying")
    void etbCreatesSpiritToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new HauntedDead(), "{3}{B}");
        harness.passBothPriorities(); // resolve creature → ETB on stack
        harness.passBothPriorities(); // resolve ETB

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(token.getCard().getColors()).containsExactly(CardColor.WHITE);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Graveyard ability returns Haunted Dead tapped after discarding two cards")
    void graveyardAbilityReturnsTappedAfterDiscardingTwo() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setGraveyard(player1, List.of(new HauntedDead()));
        harness.setHand(player1, List.of(new HauntedDead(), new TravelersAmulet()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0); // first discard
        harness.handleCardChosen(player1, 0); // second discard
        harness.passBothPriorities(); // resolve return
        harness.passBothPriorities(); // resolve ETB Spirit token

        Permanent haunted = findPermanent(player1, "Haunted Dead");
        assertThat(haunted.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Haunted Dead", "Traveler's Amulet");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate graveyard ability with fewer than two cards in hand")
    void cannotActivateWithFewerThanTwoCards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setGraveyard(player1, List.of(new HauntedDead()));
        harness.setHand(player1, List.of(new TravelersAmulet()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Haunted Dead");
    }

    @Test
    @DisplayName("Only the activating copy returns, and discard costs are paid before resolution")
    void returnsOnlyActivatingCopy() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        HauntedDead source = new HauntedDead();
        HauntedDead otherCopy = new HauntedDead();
        HauntedDead discardedCopy = new HauntedDead();
        TravelersAmulet discardedArtifact = new TravelersAmulet();
        harness.setGraveyard(player1, List.of(source, otherCopy));
        harness.setHand(player1, List.of(discardedCopy, discardedArtifact));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(source, otherCopy, discardedCopy, discardedArtifact);
        harness.assertNotOnBattlefield(player1, "Haunted Dead");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Haunted Dead").getCard().getId()).isEqualTo(source.getId());
        assertThat(countPermanents(player1, "Haunted Dead")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(otherCopy, discardedCopy, discardedArtifact);
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("The graveyard ability can be activated during an opponent's end step")
    void canReturnDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.setGraveyard(player1, List.of(new HauntedDead()));
        harness.setHand(player1, List.of(new TravelersAmulet(), new TravelersAmulet()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Haunted Dead").isTapped()).isTrue();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("No creature or Spirit returns if the source leaves the graveyard before resolution")
    void sourceMustStillBeInGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        HauntedDead source = new HauntedDead();
        HauntedDead otherCopy = new HauntedDead();
        TravelersAmulet firstDiscard = new TravelersAmulet();
        TravelersAmulet secondDiscard = new TravelersAmulet();
        harness.setGraveyard(player1, List.of(source, otherCopy));
        harness.setHand(player1, List.of(firstDiscard, secondDiscard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.setGraveyard(player1, List.of(otherCopy, firstDiscard, secondDiscard));
        harness.setExile(player1, List.of(source));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(otherCopy, firstDiscard, secondDiscard);
        assertThat(gd.findExiledCard(source.getId())).isNotNull();
    }

    @Test
    @DisplayName("Colorless mana cannot pay the black requirement of the graveyard ability")
    void cannotActivateWithoutBlackMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        HauntedDead source = new HauntedDead();
        TravelersAmulet firstDiscard = new TravelersAmulet();
        TravelersAmulet secondDiscard = new TravelersAmulet();
        harness.setGraveyard(player1, List.of(source));
        harness.setHand(player1, List.of(firstDiscard, secondDiscard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDiscard, secondDiscard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        assertThat(gd.stack).isEmpty();
    }
}
