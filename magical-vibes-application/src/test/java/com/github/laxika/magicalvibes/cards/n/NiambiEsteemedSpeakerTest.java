package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NiambiEsteemedSpeaker.class, ArvadTheCursed.class, GrizzlyBears.class, HillGiant.class, Shock.class})
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
    @DisplayName("The ETB has no legal target when only an opponent's creature is available")
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

    @Test
    @DisplayName("Niambi can be cast during the opponent's combat")
    void flashAllowsCastingDuringOpponentCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new NiambiEsteemedSpeaker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Niambi, Esteemed Speaker");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A creature destroyed in response is not returned and grants no life")
    void removedTargetDoesNotGrantLife() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        castNiambi();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Discard and tap are paid before the draw ability resolves")
    void activationPaysCostsBeforeDrawing() {
        addReadyNiambi();
        harness.setHand(player1, List.of(new NiambiEsteemedSpeaker()));
        harness.setLibrary(player1, List.of(new NiambiEsteemedSpeaker(), new NiambiEsteemedSpeaker()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Niambi, Esteemed Speaker");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Summoning sickness prevents activating Niambi's tap ability")
    void summoningSicknessPreventsActivation() {
        addReadyNiambi();
        gd.playerBattlefields.get(player1.getId()).getFirst().setSummoningSick(true);
        harness.setHand(player1, List.of(new NiambiEsteemedSpeaker()));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.assertInHand(player1, "Niambi, Esteemed Speaker");
    }

    @Test
    @DisplayName("A tapped Niambi cannot activate its draw ability")
    void tappedNiambiCannotActivate() {
        addReadyNiambi();
        gd.playerBattlefields.get(player1.getId()).getFirst().setTapped(true);
        harness.setHand(player1, List.of(new NiambiEsteemedSpeaker()));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        harness.assertInHand(player1, "Niambi, Esteemed Speaker");
    }

    @Test
    @DisplayName("The activation requires the generic mana in addition to white and blue")
    void activationRequiresFullManaCost() {
        addReadyNiambi();
        harness.setHand(player1, List.of(new NiambiEsteemedSpeaker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Niambi, Esteemed Speaker");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
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
        Permanent niambi = harness.addToBattlefieldAndReturn(player1, new NiambiEsteemedSpeaker());
        niambi.setSummoningSick(false);
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
