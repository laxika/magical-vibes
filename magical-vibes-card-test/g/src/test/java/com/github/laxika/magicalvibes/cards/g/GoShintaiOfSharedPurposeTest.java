package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HondenOfSeeingWinds;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoShintaiOfSharedPurpose.class})
class GoShintaiOfSharedPurposeTest extends BaseCardTest {

    @Test
    @DisplayName("At your end step, paying {1} creates a Spirit for each Shrine you control")
    @CardUsed({HondenOfSeeingWinds.class})
    void paysToCreateOneSpiritPerShrine() {
        harness.addToBattlefield(player1, new GoShintaiOfSharedPurpose());
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    @DisplayName("Declining the payment creates no Spirits")
    @CardUsed({HondenOfSeeingWinds.class})
    void declinesPayment() {
        harness.addToBattlefield(player1, new GoShintaiOfSharedPurpose());
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("The ability triggers only during its controller's end step")
    void triggersOnlyOnControllersEndStep() {
        harness.addToBattlefield(player1, new GoShintaiOfSharedPurpose());

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNull();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("The source counts itself and creates a colorless 1/1 Spirit creature token")
    void countsItselfAndCreatesCorrectToken() {
        harness.addToBattlefield(player1, new GoShintaiOfSharedPurpose());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Spirit")).singleElement().satisfies(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
            assertThat(token.getCard().getColors()).isEmpty();
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.isTapped()).isFalse();
        });
    }

    @Test
    @DisplayName("Opposing Shrines do not increase the number of Spirits")
    void doesNotCountOpponentsShrines() {
        harness.addToBattlefield(player1, new GoShintaiOfSharedPurpose());
        harness.addToBattlefield(player2, new GoShintaiOfSharedPurpose());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("A pending trigger survives its source leaving and counts only the remaining Shrines")
    @CardUsed({HondenOfSeeingWinds.class})
    void countsRemainingShrinesAfterSourceLeaves() {
        harness.addToBattlefield(player1, new GoShintaiOfSharedPurpose());
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());

        advanceToEndStep(player1);
        var source = findPermanent(player1, "Go-Shintai of Shared Purpose");
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("Accepting without mana cannot create Spirits")
    void cannotCreateTokensWithoutPaying() {
        harness.addToBattlefield(player1, new GoShintaiOfSharedPurpose());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }
}
