package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KadenaSlinkingSorcerer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanctumOfEternity.class, KadenaSlinkingSorcerer.class})
class SanctumOfEternityTest extends BaseCardTest {

    @Test
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new SanctumOfEternity());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void returnsOwnedCommanderToHand() {
        Card commanderCard = new KadenaSlinkingSorcerer();
        commanderCard.setOwnerId(player1.getId());
        gd.makeCommander(player1.getId(), commanderCard);
        gd.playerCommandZones.get(player1.getId()).clear();

        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new SanctumOfEternity());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, commanderCard);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, commander.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(commander);
        assertThat(gd.playerHands.get(player1.getId())).contains(commanderCard);
        assertThat(sanctum.isTapped()).isTrue();
    }

    @Test
    void rejectsOpponentCommander() {
        Card opponentCommanderCard = new KadenaSlinkingSorcerer();
        opponentCommanderCard.setOwnerId(player2.getId());
        gd.makeCommander(player2.getId(), opponentCommanderCard);
        gd.playerCommandZones.get(player2.getId()).clear();
        Permanent opponentCommander = harness.addToBattlefieldAndReturn(player2, opponentCommanderCard);
        harness.addToBattlefield(player1, new SanctumOfEternity());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentCommander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    void rejectsOwnedNonCommander() {
        harness.addToBattlefield(player1, new SanctumOfEternity());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KadenaSlinkingSorcerer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsReturnAbilityDuringOpponentsTurn() {
        Card card = new KadenaSlinkingSorcerer();
        card.setOwnerId(player1.getId());
        gd.makeCommander(player1.getId(), card);
        gd.playerCommandZones.get(player1.getId()).clear();
        harness.addToBattlefield(player1, new SanctumOfEternity());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, card);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, commander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void manaAbilityWorksDuringOpponentsTurn() {
        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new SanctumOfEternity());
        harness.forceActivePlayer(player2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(sanctum.isTapped()).isTrue();
    }

    @Test
    void returnsOwnedCommanderControlledByOpponent() {
        Card card = new KadenaSlinkingSorcerer();
        card.setOwnerId(player1.getId());
        gd.makeCommander(player1.getId(), card);
        gd.playerCommandZones.get(player1.getId()).clear();
        harness.addToBattlefield(player1, new SanctumOfEternity());
        Permanent commander = harness.addToBattlefieldAndReturn(player2, card);
        gd.stolenCreatures.put(commander.getId(), player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, commander.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(commander);
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(card);
    }

    @Test
    void ownerMayReturnCommanderToCommandZoneInstead() {
        Card card = new KadenaSlinkingSorcerer();
        card.setOwnerId(player1.getId());
        gd.makeCommander(player1.getId(), card);
        gd.playerCommandZones.get(player1.getId()).clear();
        harness.addToBattlefield(player1, new SanctumOfEternity());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, card);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, commander.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(commander);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(card);
        assertThat(gd.playerCommandZones.get(player1.getId())).contains(card);
    }
    @Test
    void returnAbilityWorksDuringOwnEndStep() {
        Card card = new KadenaSlinkingSorcerer();
        card.setOwnerId(player1.getId());
        gd.makeCommander(player1.getId(), card);
        gd.playerCommandZones.get(player1.getId()).clear();
        harness.addToBattlefield(player1, new SanctumOfEternity());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, card);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.END_STEP);

        harness.activateAbility(player1, 0, 1, null, commander.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(commander);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void cannotReturnCommanderWithoutTwoMana() {
        Card card = new KadenaSlinkingSorcerer();
        card.setOwnerId(player1.getId());
        gd.makeCommander(player1.getId(), card);
        gd.playerCommandZones.get(player1.getId()).clear();
        harness.addToBattlefield(player1, new SanctumOfEternity());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, card);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, commander.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(commander);
    }
}
