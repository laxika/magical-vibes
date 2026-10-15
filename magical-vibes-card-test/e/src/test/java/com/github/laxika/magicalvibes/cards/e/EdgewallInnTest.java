package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BeanstalkWurm;
import com.github.laxika.magicalvibes.cards.p.PlantBeans;
import com.github.laxika.magicalvibes.cards.r.RatOut;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EdgewallInn.class, BeanstalkWurm.class, PlantBeans.class, RatOut.class})
class EdgewallInnTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and stores the chosen color")
    void entersTappedAndStoresChosenColor() {
        harness.setHand(player1, List.of(new EdgewallInn()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        Permanent inn = findPermanent(player1, "Edgewall Inn");
        assertThat(inn.isTapped()).isTrue();
        assertThat(inn.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("Tapping adds one mana of the chosen color")
    void tappingAddsChosenColorMana() {
        Permanent inn = addReadyInn(player1, CardColor.GREEN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(inn.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Returns a targeted Adventure card to hand and sacrifices itself")
    void returnsAdventureCardFromGraveyardToHand() {
        Permanent inn = addReadyInn(player1, CardColor.BLUE);
        Card adventure = new BeanstalkWurm();
        harness.setGraveyard(player1, List.of(adventure));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, adventure.getId(), Zone.GRAVEYARD);
        harness.assertInGraveyard(player1, "Edgewall Inn");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(adventure.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(adventure.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent == inn);
    }

    @Test
    @DisplayName("Cannot target a card without Adventure")
    void cannotTargetCardWithoutAdventure() {
        addReadyInn(player1, CardColor.BLUE);
        Card ordinaryCard = new RatOut();
        harness.setGraveyard(player1, List.of(ordinaryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, ordinaryCard.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyInn(com.github.laxika.magicalvibes.model.Player player, CardColor chosenColor) {
        Permanent inn = harness.addToBattlefieldAndReturn(player, new EdgewallInn());
        inn.setSummoningSick(false);
        inn.setChosenColor(chosenColor);
        return inn;
    }

    @Test
    @DisplayName("Cannot return an Adventure card from an opponent's graveyard")
    void cannotTargetOpponentsAdventureCard() {
        addReadyInn(player1, CardColor.BLUE);
        Card adventure = new BeanstalkWurm();
        harness.setGraveyard(player2, List.of(adventure));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, adventure.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Edgewall Inn");
        harness.assertInGraveyard(player2, "Beanstalk Wurm");
    }

    @Test
    @DisplayName("Return ability requires three mana in addition to tapping and sacrificing")
    void cannotActivateReturnWithInsufficientMana() {
        Permanent inn = addReadyInn(player1, CardColor.BLUE);
        Card adventure = new BeanstalkWurm();
        harness.setGraveyard(player1, List.of(adventure));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, adventure.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Edgewall Inn");
        assertThat(inn.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Beanstalk Wurm");
    }

    @Test
    @DisplayName("Tapped Inn cannot activate either ability")
    void tappedInnCannotActivateAbilities() {
        Permanent inn = addReadyInn(player1, CardColor.GREEN);
        inn.tap();
        Card adventure = new BeanstalkWurm();
        harness.setGraveyard(player1, List.of(adventure));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, adventure.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Edgewall Inn");
        harness.assertInGraveyard(player1, "Beanstalk Wurm");
    }

    @Test
    @DisplayName("Return ability does not return a target that leaves the graveyard before resolution")
    void targetLeavingGraveyardDoesNotReturn() {
        addReadyInn(player1, CardColor.BLUE);
        Card adventure = new BeanstalkWurm();
        harness.setGraveyard(player1, List.of(adventure));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, adventure.getId(), Zone.GRAVEYARD);
        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Edgewall Inn");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        gd.playerGraveyards.get(player1.getId()).remove(adventure);
        harness.setExile(player1, List.of(adventure));
        resolveAllTriggers();

        harness.assertNotInHand(player1, "Beanstalk Wurm");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(adventure.getId()));
        harness.assertNotOnBattlefield(player1, "Edgewall Inn");
        harness.assertInGraveyard(player1, "Edgewall Inn");
    }
}
