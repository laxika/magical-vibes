package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SauronLordOfTheRings.class, GrizzlyBears.class, Forest.class, Shock.class})
class SauronLordOfTheRingsTest extends BaseCardTest {

    @Test
    void castingSauronAmassesMillsAndReturnsACreature() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new SauronLordOfTheRings()));
        addSauronMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent army = findPermanent(player1, "Orc Army");
        assertThat(army.getCard().getSubtypes()).containsExactly(CardSubtype.ORC, CardSubtype.ARMY);
        assertThat(army.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(5);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);
        assertThat(choice.mandatory()).isTrue();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    void opponentCommanderDeathTemptsTheRing() {
        harness.addToBattlefield(player1, new SauronLordOfTheRings());
        GrizzlyBears commanderCard = new GrizzlyBears();
        gd.makeCommander(player2.getId(), commanderCard);
        Permanent commander = harness.addToBattlefieldAndReturn(player2, commanderCard);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        preparePlayerOneMainPhase();

        harness.castInstant(player1, 0, commander.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CommanderReturnChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(commander);
        assertThat(gd.ringLevels).containsEntry(player1.getId(), 1);
        assertThat(gd.ringBearerIds).containsKey(player1.getId());
    }

    @Test
    void noncommanderCreatureDeathDoesNotTemptTheRing() {
        harness.addToBattlefield(player1, new SauronLordOfTheRings());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        preparePlayerOneMainPhase();

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.ringLevels).doesNotContainKey(player1.getId());
    }

    private void addSauronMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void preparePlayerOneMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
