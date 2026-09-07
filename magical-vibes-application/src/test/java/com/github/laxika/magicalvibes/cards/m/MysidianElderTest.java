package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysidianElder.class, GrizzlyBears.class, Shock.class})
class MysidianElderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and creates a Wizard token")
    void createsWizardTokenOnEntry() {
        castMysidianElder();

        Permanent wizard = findPermanent(player1, "Wizard");
        assertThat(wizard.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("The Wizard token deals damage when you cast a noncreature spell")
    void wizardTokenDamagesEachOpponentForNoncreatureSpell() {
        castMysidianElder();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The Wizard token does not trigger for a creature spell")
    void wizardTokenDoesNotTriggerForCreatureSpell() {
        castMysidianElder();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void castMysidianElder() {
        harness.setHand(player1, List.of(new MysidianElder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
