package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrakeHaven.class, Censor.class, TormentingVoice.class})
class DrakeHavenTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card and paying {1} creates a 2/2 blue Drake with flying")
    void cyclePayCreatesDrake() {
        harness.addToBattlefield(player1, new DrakeHaven());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Censor()));
        harness.addMana(player1, ManaColor.BLUE, 1);      // cycling {U}
        harness.addMana(player1, ManaColor.COLORLESS, 1); // the may-pay {1}

        harness.activateHandAbility(player1, 0, null); // cycle Censor -> discard trigger
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().isToken()
                        && p.getCard().getPower() == 2
                        && p.getCard().getToughness() == 2
                        && p.getCard().getColor() == CardColor.BLUE
                        && p.getCard().getSubtypes().contains(CardSubtype.DRAKE)
                        && p.getCard().getKeywords().contains(Keyword.FLYING));
    }

    @Test
    @DisplayName("Declining the may-pay creates no Drake")
    void declineCreatesNoDrake() {
        harness.addToBattlefield(player1, new DrakeHaven());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Censor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken() && p.getCard().getSubtypes().contains(CardSubtype.DRAKE));
    }

    @Test
    @DisplayName("Cycling triggers once and creates the Drake before drawing")
    void cyclingTriggersOnceBeforeDrawing() {
        harness.addToBattlefield(player1, new DrakeHaven());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Censor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getSubtypes().contains(CardSubtype.DRAKE)))
                .hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream().filter(p -> p.getCard().isToken()))
                .hasSize(1);
    }

    @Test
    @DisplayName("Discarding without cycling also triggers Drake Haven")
    void ordinaryDiscardCreatesDrake() {
        harness.addToBattlefield(player1, new DrakeHaven());
        harness.setHand(player1, List.of(new TormentingVoice(), new Censor()));
        harness.setLibrary(player1, List.of(new Censor(), new Censor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream().filter(p -> p.getCard().isToken()))
                .hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An opponent cycling does not trigger your Drake Haven")
    void opponentCyclingDoesNotTrigger() {
        harness.addToBattlefield(player1, new DrakeHaven());
        harness.setHand(player2, List.of(new Censor()));
        harness.setLibrary(player2, List.of(new Censor()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player2, 0, null);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());
    }

    @Test
    @DisplayName("Each Drake Haven offers its own payment for the same discarded card")
    void multipleHavensHaveIndependentPayments() {
        harness.addToBattlefield(player1, new DrakeHaven());
        harness.addToBattlefield(player1, new DrakeHaven());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Censor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream().filter(p -> p.getCard().isToken()))
                .hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Accepting without enough mana does not create a Drake")
    void unpaidAbilityCreatesNoDrake() {
        harness.addToBattlefield(player1, new DrakeHaven());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Censor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
