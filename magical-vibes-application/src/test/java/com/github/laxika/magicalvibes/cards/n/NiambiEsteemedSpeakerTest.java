package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NiambiEsteemedSpeakerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns another creature and gains life equal to its mana value")
    void etbReturnsCreatureAndGainsItsManaValue() {
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setLife(player1, 20);
        castNiambi();

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, hillGiant.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Hill Giant");
        harness.assertOnBattlefield(player1, "Niambi, Esteemed Speaker");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Declining the ETB leaves the creature and life total unchanged")
    void decliningEtbDoesNothing() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        castNiambi();

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Niambi, Esteemed Speaker");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The ETB does not trigger when only an opponent's creature is available")
    void etbCannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        castNiambi();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Niambi, Esteemed Speaker");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The activated ability discards a legendary card and draws two cards")
    void activatedAbilityDiscardsLegendaryAndDrawsTwo() {
        addReadyNiambi();
        harness.setHand(player1, List.of(new ArvadTheCursed()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant()));
        harness.setLife(player1, 20);
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Arvad the Cursed");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability cannot discard a nonlegendary card")
    void activatedAbilityRequiresLegendaryCard() {
        addReadyNiambi();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castNiambi() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NiambiEsteemedSpeaker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
    }

    private void addReadyNiambi() {
        Permanent niambi = new Permanent(new NiambiEsteemedSpeaker());
        niambi.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(niambi);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
