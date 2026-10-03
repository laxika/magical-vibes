package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.b.BarrinsCodex;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CopperGnomes.class, BarrinsCodex.class, ArgothianSwine.class})
class CopperGnomesTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and puts an artifact card from hand onto the battlefield")
    void putsArtifactFromHandOntoBattlefield() {
        harness.addToBattlefield(player1, new CopperGnomes());
        harness.setHand(player1, List.of(new BarrinsCodex()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        activate();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Copper Gnomes");
        harness.assertOnBattlefield(player1, "Barrin's Codex");
    }

    @Test
    @DisplayName("Offers only artifact cards from hand")
    void offersOnlyArtifactCards() {
        harness.addToBattlefield(player1, new CopperGnomes());
        harness.setHand(player1, List.of(new ArgothianSwine(), new BarrinsCodex()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        activate();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Declining leaves the artifact card in hand after sacrificing itself")
    void decliningLeavesArtifactInHand() {
        harness.addToBattlefield(player1, new CopperGnomes());
        harness.setHand(player1, List.of(new BarrinsCodex()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        activate();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Copper Gnomes");
        harness.assertInHand(player1, "Barrin's Codex");
        harness.assertNotOnBattlefield(player1, "Barrin's Codex");
    }

    @Test
    @DisplayName("Accepting with no artifact card in hand does not put a card onto the battlefield")
    void acceptingWithNoArtifactInHandDoesNothing() {
        harness.addToBattlefield(player1, new CopperGnomes());
        harness.setHand(player1, List.of(new ArgothianSwine()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        activate();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Copper Gnomes");
        harness.assertInHand(player1, "Argothian Swine");
        harness.assertNotOnBattlefield(player1, "Argothian Swine");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void activate() {
        harness.activateAbility(player1, 0, null, null);
    }

    @Test
    @DisplayName("Sacrifice is paid before the ability resolves")
    void sacrificesImmediatelyAsActivationCost() {
        harness.addToBattlefield(player1, new CopperGnomes());
        harness.setHand(player1, List.of(new BarrinsCodex()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        activate();

        harness.assertNotOnBattlefield(player1, "Copper Gnomes");
        harness.assertInGraveyard(player1, "Copper Gnomes");
        harness.assertInHand(player1, "Barrin's Codex");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Barrin's Codex");
        harness.assertNotInHand(player1, "Barrin's Codex");
    }

    @Test
    @DisplayName("Three mana cannot pay the activation cost and does not sacrifice the Gnomes")
    void cannotActivateWithInsufficientMana() {
        harness.addToBattlefield(player1, new CopperGnomes());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(this::activate).isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Copper Gnomes");
        harness.assertNotInGraveyard(player1, "Copper Gnomes");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapped summoning-sick Gnomes can activate and put an artifact creature onto the battlefield")
    void canActivateWhileTappedAndSummoningSick() {
        var gnomes = harness.addToBattlefieldAndReturn(player1, new CopperGnomes());
        gnomes.setTapped(true);
        gnomes.setSummoningSick(true);
        harness.setHand(player1, List.of(new CopperGnomes(), new BarrinsCodex()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        activate();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Copper Gnomes");
        harness.assertInGraveyard(player1, "Copper Gnomes");
        harness.assertInHand(player1, "Barrin's Codex");
        harness.assertNotOnBattlefield(player1, "Barrin's Codex");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
