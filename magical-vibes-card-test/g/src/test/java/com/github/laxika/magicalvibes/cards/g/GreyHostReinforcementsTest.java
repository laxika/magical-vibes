package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreyHostReinforcements.class, GrizzlyBears.class, Shock.class})
class GreyHostReinforcementsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target graveyard and gets one counter per creature card exiled")
    void exilesGraveyardAndCountsCreatureCards() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        Card noncreature = new Shock();
        harness.setGraveyard(player2, List.of(firstCreature, secondCreature, noncreature));
        castGreyHost(player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(firstCreature, secondCreature, noncreature);
        assertThat(greyHost().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets no counters when the target graveyard has no creature cards")
    void noCreatureCardsMeansNoCounters() {
        Card noncreature = new Shock();
        harness.setGraveyard(player1, List.of(noncreature));
        castGreyHost(player1.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(noncreature);
        assertThat(greyHost().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castGreyHost(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new GreyHostReinforcements()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, targetPlayerId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent greyHost() {
        return findPermanent(player1, "Grey Host Reinforcements");
    }

    @Test
    @DisplayName("An empty graveyard is a legal target and gives no counters")
    void emptyGraveyardGivesNoCounters() {
        harness.setGraveyard(player2, List.of());

        castGreyHost(player2.getId());

        assertThat(greyHost().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiling your own graveyard counts its creatures and leaves the opponent's graveyard alone")
    void targetsOnlyChosenPlayersGraveyard() {
        Card ownCreature = new GrizzlyBears();
        Card ownSpell = new Shock();
        Card opposingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCreature, ownSpell));
        harness.setGraveyard(player2, List.of(opposingCreature));

        castGreyHost(player1.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(ownCreature, ownSpell);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(greyHost().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts creature cards that enter the graveyard in response to the trigger")
    void countsGraveyardAtResolution() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GreyHostReinforcements(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bear.getCard());
        assertThat(greyHost().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The graveyard is still exiled if Grey Host leaves before its trigger resolves")
    void exilesGraveyardAfterSourceDies() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new GreyHostReinforcements(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        Card source = greyHost().getCard();

        harness.castAndResolveInstant(player1, 0, greyHost().getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grey Host Reinforcements");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("Ward counters an opposing spell when its controller cannot pay three mana")
    void wardCountersSpellWithoutPayment() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GreyHostReinforcements());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grey Host Reinforcements");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying three mana for ward allows an opposing spell to resolve")
    void payingWardAllowsSpellToResolve() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GreyHostReinforcements());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grey Host Reinforcements");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source.getCard());
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
