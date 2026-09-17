package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmaugTheImpenetrable.class, Shock.class, GrizzlyBears.class})
class SmaugTheImpenetrableTest extends BaseCardTest {

    @Test
    @DisplayName("Noncombat damage creates that many Treasures")
    void noncombatDamageCreatesTreasureEqualToDamage() {
        Permanent smaug = addCreatureReady(player1, new SmaugTheImpenetrable());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, smaug.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("Combat damage does not trigger Smaug")
    void combatDamageDoesNotCreateTreasure() {
        Permanent smaug = addCreatureReady(player1, new SmaugTheImpenetrable());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(smaug.getMarkedDamage()).isEqualTo(2);
    }
}
