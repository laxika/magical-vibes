package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InsatiableGorgers.class, RavensCrime.class})
class InsatiableGorgersTest extends BaseCardTest {

    @Test
    @DisplayName("Must attack each combat when able")
    void mustAttackWhenAble() {
        addReadyGorgers(player1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Discarding it offers its madness cost")
    void discardTriggersMadness() {
        InsatiableGorgers gorgers = discardViaRavensCrime();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(gorgers.getId()));
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getLast().getDescription()).contains("madness");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining madness puts it into the graveyard")
    void decliningMadnessGoesToGraveyard() {
        InsatiableGorgers gorgers = discardViaRavensCrime();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(gorgers.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(gorgers.getId()));
    }

    @Test
    @DisplayName("Accepting madness pays {3}{R} and puts it onto the battlefield")
    void acceptingMadnessCastsCreature() {
        InsatiableGorgers gorgers = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(gorgers.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A tapped Gorgers is not required to attack")
    void tappedGorgersDoesNotHaveToAttack() {
        Permanent permanent = addReadyGorgers(player1);
        permanent.tap();

        assertThatCode(() -> declareAttackers(player1, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A summoning-sick Gorgers is not required to attack")
    void summoningSickGorgersDoesNotHaveToAttack() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new InsatiableGorgers());
        permanent.setSummoningSick(true);

        assertThatCode(() -> declareAttackers(player1, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Madness cannot be paid with only colorless mana")
    void madnessWithoutRedManaGoesToGraveyard() {
        InsatiableGorgers gorgers = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(gorgers.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(gorgers.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(gorgers.getId()));
    }

    private Permanent addReadyGorgers(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new InsatiableGorgers());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private InsatiableGorgers discardViaRavensCrime() {
        InsatiableGorgers gorgers = new InsatiableGorgers();
        harness.setHand(player1, List.of(gorgers));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return gorgers;
    }
}
