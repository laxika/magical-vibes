package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DwarvenTrader;
import com.github.laxika.magicalvibes.cards.s.SeaTroll;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiantAlbatross.class, DwarvenTrader.class, SeaTroll.class})
class GiantAlbatrossTest extends BaseCardTest {

    /**
     * Blocks a lethal 5/5 creature with Giant Albatross and advances to combat damage so the
     * Albatross dies to that creature's damage, then resolves the death trigger up to its may-pay
     * prompt.
     */
    private Permanent killByCreatureUntilMayPrompt(Card creature) {
        Permanent albatross = addCreatureReady(player1, new GiantAlbatross());
        albatross.setBlocking(true);
        albatross.addBlockingTarget(0);

        creature.setPower(5);
        creature.setToughness(5);
        Permanent attacker = addCreatureReady(player2, creature);
        attacker.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();

        addAlbatrossPaymentMana();
        return attacker;
    }

    private void addAlbatrossPaymentMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Pay {1}{U}; the damaging creature's controller declines 2 life, so it is destroyed")
    void payThenDamagerControllerDeclines() {
        killByCreatureUntilMayPrompt(new DwarvenTrader());

        harness.assertInGraveyard(player1, "Giant Albatross");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Dwarven Trader");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Pay {1}{U}; the damaging creature's controller pays 2 life to save it")
    void payThenDamagerControllerPaysLife() {
        killByCreatureUntilMayPrompt(new DwarvenTrader());

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Dwarven Trader");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Decline {1}{U}: nothing is destroyed")
    void declinePayingLeavesDamagerAlone() {
        killByCreatureUntilMayPrompt(new DwarvenTrader());

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Dwarven Trader");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A controller who can't pay 2 life gets no choice — the creature is destroyed")
    void controllerWhoCannotPayLosesTheCreature() {
        killByCreatureUntilMayPrompt(new DwarvenTrader());
        gd.playerLifeTotals.put(player2.getId(), 1);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Dwarven Trader");
        harness.assertLife(player2, 1);
    }

    @Test
    @DisplayName("Paying the mana offers an independent life choice for each damaging creature")
    void offersOneChoicePerDamagingCreature() {
        Permanent albatross = addCreatureReady(player1, new GiantAlbatross());
        albatross.setAttacking(true);

        DwarvenTrader firstBlockerCard = new DwarvenTrader();
        firstBlockerCard.setPower(5);
        firstBlockerCard.setToughness(5);
        Permanent firstBlocker = addCreatureReady(player2, firstBlockerCard);
        firstBlocker.setBlocking(true);
        firstBlocker.addBlockingTarget(0);

        DwarvenTrader secondBlockerCard = new DwarvenTrader();
        secondBlockerCard.setPower(5);
        secondBlockerCard.setToughness(5);
        Permanent secondBlocker = addCreatureReady(player2, secondBlockerCard);
        secondBlocker.setBlocking(true);
        secondBlocker.addBlockingTarget(0);

        resolveCombat(player1);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(firstBlocker.getId(), 1));
        harness.passBothPriorities();
        addAlbatrossPaymentMana();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player2, "Dwarven Trader")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Dwarven Trader");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A creature destroyed by the ability is not saved by a regeneration shield")
    void destructionIgnoresRegenerationShield() {
        Permanent troll = killByCreatureUntilMayPrompt(new SeaTroll());
        troll.setRegenerationShield(1);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Sea Troll");
        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }
}
