package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.SiftThroughSands;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NezumiGraverobber.class, NighteyesTheDesecrator.class,
        WanderingOnes.class, SiftThroughSands.class})
class NezumiGraverobberTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the targeted card and flips when that graveyard is left empty")
    void exilesLastCardAndFlips() {
        Permanent graverobber = addCreatureReady(player1, new NezumiGraverobber());
        Card targetCard = new WanderingOnes();
        harness.setGraveyard(player2, List.of(targetCard));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(graverobber);
        harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(targetCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(targetCard.getId()));
        assertThat(graverobber.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not flip while cards remain in that graveyard")
    void doesNotFlipWhenGraveyardStillHasCards() {
        Permanent graverobber = addCreatureReady(player1, new NezumiGraverobber());
        Card targetCard = new WanderingOnes();
        Card remainingCard = new SiftThroughSands();
        harness.setGraveyard(player2, List.of(targetCard, remainingCard));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(graverobber);
        harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(targetCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId).containsExactly(remainingCard.getId());
        assertThat(graverobber.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a card in the controller's own graveyard")
    void cannotTargetOwnGraveyard() {
        Permanent graverobber = addCreatureReady(player1, new NezumiGraverobber());
        Card targetCard = new WanderingOnes();
        harness.setGraveyard(player1, List.of(targetCard));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(graverobber);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, index, 0,
                List.of(targetCard.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent's graveyard");
    }

    @Test
    @DisplayName("Flipped side reanimates target creature card from any graveyard under your control")
    void flippedSideReanimatesFromAnyGraveyard() {
        Permanent graverobber = addCreatureReady(player1, new NezumiGraverobber());
        Card targetCard = new WanderingOnes();
        harness.setGraveyard(player2, List.of(targetCard));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(graverobber);
        harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(targetCard.getId()));
        harness.passBothPriorities();
        assertThat(graverobber.isTransformed()).isTrue();

        Card reanimatedCreature = new WanderingOnes();
        harness.setGraveyard(player2, List.of(reanimatedCreature));
        harness.addMana(player1, ManaColor.BLACK, 5);

        index = gd.playerBattlefields.get(player1.getId()).indexOf(graverobber);
        harness.activateAbility(player1, index, 0, null, reanimatedCreature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(reanimatedCreature.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate the front side without a card in an opponent's graveyard")
    void cannotActivateWithoutOpponentGraveyardCard() {
        Permanent graverobber = addCreatureReady(player1, new NezumiGraverobber());
        harness.addMana(player1, ManaColor.BLACK, 2);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(graverobber);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flipped side can return a creature card from its controller's graveyard")
    void flippedSideReanimatesFromControllerGraveyard() {
        Permanent graverobber = addCreatureReady(player1, new NezumiGraverobber());
        Card cardToFlip = new WanderingOnes();
        harness.setGraveyard(player2, List.of(cardToFlip));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(graverobber);
        harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(cardToFlip.getId()));
        harness.passBothPriorities();

        Card targetCard = new WanderingOnes();
        harness.setGraveyard(player1, List.of(targetCard));
        harness.addMana(player1, ManaColor.BLACK, 5);

        index = gd.playerBattlefields.get(player1.getId()).indexOf(graverobber);
        harness.activateAbility(player1, index, 0, null, targetCard.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(targetCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Flipped side cannot target a noncreature card")
    void flippedSideRejectsNoncreatureCard() {
        Permanent graverobber = addCreatureReady(player1, new NezumiGraverobber());
        Card cardToFlip = new WanderingOnes();
        harness.setGraveyard(player2, List.of(cardToFlip));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(graverobber);
        harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(cardToFlip.getId()));
        harness.passBothPriorities();

        Card noncreatureCard = new SiftThroughSands();
        harness.setGraveyard(player2, List.of(noncreatureCard));
        harness.addMana(player1, ManaColor.BLACK, 5);

        int flippedIndex = gd.playerBattlefields.get(player1.getId()).indexOf(graverobber);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, flippedIndex, 0, null, noncreatureCard.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
