package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeylineTyrant.class, GrizzlyBears.class})
class LeylineTyrantTest extends BaseCardTest {

    private void killTyrant() {
        Permanent tyrant = harness.addToBattlefieldAndReturn(player1, new LeylineTyrant());
        tyrant.setBlocking(true);
        tyrant.addBlockingTarget(0);

        GrizzlyBears attackerCard = new GrizzlyBears();
        attackerCard.setPower(5);
        attackerCard.setToughness(5);
        Permanent attacker = new Permanent(attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("After it dies, paying red mana deals that much damage to the chosen player")
    void deathTriggerPaysOnlyRedManaAndDealsDamage() {
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        killTyrant();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Choosing zero red mana deals no damage and keeps the mana")
    void payingZeroDealsNoDamage() {
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 2);

        killTyrant();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Unspent red mana remains through a step boundary")
    void retainsUnspentRedMana() {
        harness.addToBattlefield(player1, new LeylineTyrant());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        advanceToUpkeep(player1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
