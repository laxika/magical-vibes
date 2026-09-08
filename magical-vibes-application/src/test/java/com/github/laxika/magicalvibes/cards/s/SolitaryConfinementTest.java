package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BeaconOfImmortality;
import com.github.laxika.magicalvibes.cards.f.FlameRift;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SolitaryConfinement.class, BeaconOfImmortality.class, FlameRift.class, Forest.class,
        GrizzlyBears.class})
class SolitaryConfinementTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a card to keep Solitary Confinement during upkeep")
    void discardsCardInsteadOfSacrificing() {
        harness.addToBattlefield(player1, new SolitaryConfinement());
        harness.setHand(player1, List.of(new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Solitary Confinement");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Sacrifices itself during upkeep when its controller has no card to discard")
    void sacrificesWhenHandIsEmpty() {
        harness.addToBattlefield(player1, new SolitaryConfinement());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Solitary Confinement");
        harness.assertInGraveyard(player1, "Solitary Confinement");
    }

    @Test
    @DisplayName("Controller skips their draw step while Solitary Confinement is on the battlefield")
    void skipsControllerDrawStep() {
        harness.addToBattlefield(player1, new SolitaryConfinement());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        int handBeforeDrawStep = gd.playerHands.get(player1.getId()).size();
        int libraryBeforeDrawStep = gd.playerDecks.get(player1.getId()).size();
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeDrawStep);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBeforeDrawStep);
    }

    @Test
    @DisplayName("Controller cannot be targeted while Solitary Confinement is on the battlefield")
    void controllerHasShroud() {
        harness.addToBattlefield(player1, new SolitaryConfinement());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BeaconOfImmortality()));
        harness.addMana(player2, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Prevents noncombat damage to its controller")
    void preventsDamageToController() {
        harness.addToBattlefield(player1, new SolitaryConfinement());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new FlameRift()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Prevents combat damage to its controller")
    void preventsCombatDamageToController() {
        harness.addToBattlefield(player1, new SolitaryConfinement());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
