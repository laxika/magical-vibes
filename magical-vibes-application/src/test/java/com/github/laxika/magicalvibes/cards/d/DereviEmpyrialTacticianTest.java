package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DereviEmpyrialTacticianTest extends BaseCardTest {

    @Test
    void entersAndMayTapOrUntapTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card derevi = derevi();
        harness.setHand(player1, List.of(derevi));
        addDereviMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void combatDamageTriggerMayTapOrUntapTargetPermanent() {
        Card derevi = derevi();
        harness.addToBattlefield(player1, derevi);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void commandZoneAbilityPutsDereviOntoBattlefield() {
        Card derevi = derevi();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), derevi);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(derevi)));
        addDereviMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        gs.activateCommandZoneAbility(gd, player1, derevi.getId(), 0);
        harness.passBothPriorities();

        assertThat(gd.playerCommandZones.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Derevi, Empyrial Tactician");
    }

    private Card derevi() {
        Card card = new DereviEmpyrialTactician();
        card.setName("Derevi, Empyrial Tactician");
        card.setType(CardType.CREATURE);
        card.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.BIRD, CardSubtype.WIZARD));
        card.setColors(List.of(CardColor.GREEN, CardColor.WHITE, CardColor.BLUE));
        card.setManaCost("{G}{W}{U}");
        card.setPower(2);
        card.setToughness(3);
        card.setOwnerId(player1.getId());
        return card;
    }

    private void addDereviMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
