package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AustereCommand;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeiferAlmasy.class, GrizzlyBears.class, Shock.class, AustereCommand.class})
class SeiferAlmasyTest extends BaseCardTest {

    @Test
    @DisplayName("A creature attacking alone gains double strike until end of turn")
    void attackingAloneGainsDoubleStrike() {
        Permanent seifer = addCreatureReady(player1, new SeiferAlmasy());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(seifer.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Attacking with multiple creatures does not grant double strike")
    void attackingWithMultipleCreaturesDoesNotGrantDoubleStrike() {
        Permanent seifer = addCreatureReady(player1, new SeiferAlmasy());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(seifer.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Combat damage targets only qualifying instants and sorceries from your graveyard")
    void combatDamageTargetsOnlyQualifyingCards() {
        Card shock = new Shock();
        Card austereCommand = new AustereCommand();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(shock, austereCommand, creature));

        dealCombatDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(shock.getId());
    }

    @Test
    @DisplayName("Combat damage casts a qualifying spell for free and exiles it")
    void combatDamageCastsSpellForFreeAndExilesIt() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setLife(player2, 20);

        dealCombatDamage();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(shock.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
    }

    private void dealCombatDamage() {
        Permanent seifer = addCreatureReady(player1, new SeiferAlmasy());
        seifer.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
