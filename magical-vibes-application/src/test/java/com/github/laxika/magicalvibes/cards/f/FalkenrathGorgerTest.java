package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CaptivatingVampire;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FalkenrathGorgerTest extends BaseCardTest {

    private void putGorgerOnBattlefield() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new FalkenrathGorger()));
    }

    /** Force player1 to discard {@code card} via Raven's Crime from player2. */
    private <T> T discardViaRavensCrime(T card) {
        harness.setHand(player1, List.of((com.github.laxika.magicalvibes.model.Card) card));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return card;
    }

    @Test
    @DisplayName("Discarding a Vampire while Gorger is on the battlefield exiles it for madness")
    void discardVampireTriggersGrantedMadness() {
        putGorgerOnBattlefield();
        CaptivatingVampire vampire = discardViaRavensCrime(new CaptivatingVampire());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(vampire.getId()));
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getLast().getDescription()).contains("madness");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining granted madness puts the Vampire into the graveyard")
    void decliningGrantedMadnessGoesToGraveyard() {
        putGorgerOnBattlefield();
        CaptivatingVampire vampire = discardViaRavensCrime(new CaptivatingVampire());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(vampire.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(vampire.getId()));
    }

    @Test
    @DisplayName("Accepting granted madness casts the Vampire for its mana cost")
    void acceptingGrantedMadnessCastsForManaCost() {
        putGorgerOnBattlefield();
        CaptivatingVampire vampire = discardViaRavensCrime(new CaptivatingVampire());
        // Captivating Vampire is {1}{B}{B}
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(vampire.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Non-Vampire discard does not get granted madness")
    void nonVampireDoesNotGetMadness() {
        putGorgerOnBattlefield();
        SerraAngel angel = discardViaRavensCrime(new SerraAngel());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(angel.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(angel.getId()));
        assertThat(gd.stack).noneMatch(e -> e.getDescription() != null && e.getDescription().contains("madness"));
    }

    @Test
    @DisplayName("Discarding Gorger itself does not grant it madness")
    void discardingGorgerDoesNotSelfGrantMadness() {
        FalkenrathGorger gorger = discardViaRavensCrime(new FalkenrathGorger());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(gorger.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(gorger.getId()));
    }

    @Test
    @DisplayName("Madness cast still works if Gorger leaves before the trigger resolves")
    void madnessSurvivesGorgerLeaving() {
        Permanent gorger = new Permanent(new FalkenrathGorger());
        gd.playerBattlefields.get(player1.getId()).add(gorger);

        CaptivatingVampire vampire = discardViaRavensCrime(new CaptivatingVampire());
        assertThat(gd.stack.getLast().getDescription()).contains("madness");

        // Remove Gorger while the madness trigger is still on the stack
        gd.playerBattlefields.get(player1.getId()).remove(gorger);

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(vampire.getId()));
    }
}
