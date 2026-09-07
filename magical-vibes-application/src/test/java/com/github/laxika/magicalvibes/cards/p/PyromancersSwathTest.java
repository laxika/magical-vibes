package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ConsumeSpirit;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PyromancersSwath.class, Shock.class, ConsumeSpirit.class, CrawWurm.class,
        ProdigalPyromancer.class, LlanowarElves.class})
class PyromancersSwathTest extends BaseCardTest {

    @Test
    @DisplayName("An instant you control deals two extra damage to a player")
    void instantDealsTwoExtraDamageToPlayer() {
        harness.addToBattlefield(player1, new PyromancersSwath());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("An opponent's instant does not get the damage bonus")
    void opponentsInstantIsNotBoosted() {
        harness.addToBattlefield(player1, new PyromancersSwath());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A sorcery you control deals two extra damage to a permanent")
    void sorceryDealsTwoExtraDamageToPermanent() {
        harness.addToBattlefield(player1, new PyromancersSwath());
        harness.addToBattlefield(player2, new CrawWurm());
        harness.setHand(player1, List.of(new ConsumeSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 2, harness.getPermanentId(player2, "Craw Wurm"));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(com.github.laxika.magicalvibes.model.Card::getName)
                .contains("Craw Wurm");
    }

    @Test
    @DisplayName("Damage from an activated ability is not boosted")
    void activatedAbilityDamageIsNotBoosted() {
        harness.addToBattlefield(player1, new PyromancersSwath());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The controller's end step discards their entire hand")
    void controllerEndStepDiscardsHand() {
        harness.addToBattlefield(player1, new PyromancersSwath());
        harness.setHand(player1, List.of(new LlanowarElves(), new LlanowarElves()));

        advanceToEndStepTrigger(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Llanowar Elves"))
                .hasSize(2);
    }

    @Test
    @DisplayName("The end-step discard does not affect an opponent's hand")
    void opponentEndStepDoesNotDiscardHand() {
        harness.addToBattlefield(player1, new PyromancersSwath());
        harness.setHand(player2, List.of(new LlanowarElves()));

        advanceToEndStepTrigger(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    private void advanceToEndStepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
