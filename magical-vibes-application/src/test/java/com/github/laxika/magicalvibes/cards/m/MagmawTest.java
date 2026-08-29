package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MagmawTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and deals 1 damage to target player")
    void sacrificesItselfAndDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new Magmaw());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Magmaw");
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Sacrifices a chosen nonland permanent and deals damage to target creature")
    void sacrificesChosenNonlandPermanentAndDealsDamageToCreature() {
        harness.addToBattlefield(player1, new Magmaw());
        harness.addToBattlefield(player1, new Pacifism());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID pacifismId = findPermanent(player1, "Pacifism").getId();
        UUID elvesId = findPermanent(player2, "Llanowar Elves").getId();

        harness.activateAbility(player1, 0, null, elvesId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, pacifismId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pacifism");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Magmaw");
    }

    @Test
    @DisplayName("Does not sacrifice a land when a nonland source is available")
    void doesNotSacrificeLand() {
        harness.addToBattlefield(player1, new Magmaw());
        harness.addToBattlefield(player1, new Mountain());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Magmaw");
        harness.assertOnBattlefield(player1, "Mountain");
    }
}
