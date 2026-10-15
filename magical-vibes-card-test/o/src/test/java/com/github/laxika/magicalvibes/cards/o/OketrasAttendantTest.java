package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({OketrasAttendant.class})
class OketrasAttendantTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new OketrasAttendant()));
        harness.setLibrary(player1, List.of(new OketrasAttendant()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Oketra's Attendant");
        harness.assertInHand(player1, "Oketra's Attendant");
    }

    private void setUpEmbalm() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new OketrasAttendant()));
        harness.addMana(player1, ManaColor.WHITE, 5);
    }

    @Test
    @DisplayName("Embalm exiles the source card from the graveyard as a cost")
    void embalmExilesSourceAsCost() {
        setUpEmbalm();

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Oketra's Attendant");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Oketra's Attendant"));
    }

    @Test
    @DisplayName("Embalm creates a white Zombie Bird Soldier token copy with no mana cost")
    void embalmCreatesWhiteZombieTokenCopy() {
        setUpEmbalm();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve the Embalm ability

        Permanent token = findPermanent(player1, "Oketra's Attendant");
        assertThat(token.getCard().isToken()).isTrue();

        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getColors()).contains(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes())
                .contains(CardSubtype.ZOMBIE, CardSubtype.BIRD, CardSubtype.SOLDIER);
        assertThat(token.getCard().getManaCost()).isEmpty();
    }

    @Test
    @DisplayName("Embalm can only be activated at sorcery speed")
    void embalmOnlyAtSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new OketrasAttendant()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        // Opponent's turn — not sorcery speed for player1.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Oketra's Attendant");
    }

    @Test
    @DisplayName("Cycling discards immediately but draws only on resolution, even on an opponent's turn")
    void cyclingPaysDiscardBeforeDrawingOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        OketrasAttendant source = new OketrasAttendant();
        OketrasAttendant drawn = new OketrasAttendant();
        harness.setHand(player1, List.of(source));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be activated with only one mana and does not discard the card")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new OketrasAttendant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Oketra's Attendant");
        harness.assertNotInGraveyard(player1, "Oketra's Attendant");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("A cycled Attendant can subsequently be embalmed using three generic and two white mana")
    void cycledCardCanBeEmbalmed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        OketrasAttendant source = new OketrasAttendant();
        harness.setHand(player1, List.of(source));
        harness.setLibrary(player1, List.of(new OketrasAttendant()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(source);
        harness.assertNotOnBattlefield(player1, "Oketra's Attendant");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Oketra's Attendant");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(source.getPower());
        assertThat(token.getCard().getToughness()).isEqualTo(source.getToughness());
        assertThat(token.getCard().getKeywords()).containsAll(source.getKeywords());
        assertThat(token.isSummoningSick()).isTrue();
        assertThat(countPermanents(player1, "Oketra's Attendant")).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Oketra's Attendant");
    }

    @Test
    @DisplayName("Embalm requires two white mana even if five total mana are available")
    void embalmRequiresTwoWhiteMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new OketrasAttendant()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Oketra's Attendant");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
    }

    @Test
    @DisplayName("Embalm requires five total mana and does not exile the card when payment fails")
    void embalmRequiresFiveMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new OketrasAttendant()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Oketra's Attendant");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("Embalm cannot be activated during its controller's upkeep")
    void embalmRequiresMainPhase() {
        setUpEmbalm();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Oketra's Attendant");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Embalm cannot respond to a cycling ability on the stack")
    void embalmRequiresEmptyStack() {
        setUpEmbalm();
        harness.setHand(player1, List.of(new OketrasAttendant()));
        harness.setLibrary(player1, List.of(new OketrasAttendant()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
    }
}
