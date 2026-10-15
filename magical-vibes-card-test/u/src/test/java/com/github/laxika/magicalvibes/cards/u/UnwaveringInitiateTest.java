package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnwaveringInitiate.class})
class UnwaveringInitiateTest extends BaseCardTest {

    private void setUpEmbalm() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new UnwaveringInitiate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Embalm exiles the source card from the graveyard as a cost")
    void embalmExilesSourceAsCost() {
        setUpEmbalm();

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Unwavering Initiate");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Unwavering Initiate"));
    }

    @Test
    @DisplayName("Embalm creates a white Zombie Human Warrior token copy with no mana cost")
    void embalmCreatesWhiteZombieTokenCopy() {
        setUpEmbalm();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve the Embalm ability

        Permanent token = findPermanent(player1, "Unwavering Initiate");

        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getColors()).contains(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes())
                .contains(CardSubtype.ZOMBIE, CardSubtype.HUMAN, CardSubtype.WARRIOR);
        assertThat(token.getCard().getManaCost()).isEmpty();
    }

    @Test
    @DisplayName("Embalm can only be activated at sorcery speed")
    void embalmOnlyAtSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new UnwaveringInitiate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        // Opponent's turn — not sorcery speed for player1.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Unwavering Initiate");
    }

    @Test
    @DisplayName("Vigilance allows the original creature to attack without tapping")
    void originalAttacksWithoutTapping() {
        Permanent initiate = addCreatureReady(player1, new UnwaveringInitiate());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(initiate.isAttacking()).isTrue();
        assertThat(initiate.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The embalmed token retains vigilance")
    void embalmedTokenAttacksWithoutTapping() {
        setUpEmbalm();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Unwavering Initiate");
        token.setSummoningSick(false);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(token.isAttacking()).isTrue();
        assertThat(token.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Embalm cannot be activated during upkeep")
    void embalmCannotBeActivatedDuringUpkeep() {
        setUpEmbalm();
        harness.forceStep(TurnStep.UPKEEP);

        Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Unwavering Initiate");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Embalm requires white mana and does not exile the card if payment fails")
    void embalmRequiresWhiteMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new UnwaveringInitiate()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Unwavering Initiate");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
