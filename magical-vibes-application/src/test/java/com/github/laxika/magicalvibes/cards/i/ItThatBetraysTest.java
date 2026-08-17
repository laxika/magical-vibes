package com.github.laxika.magicalvibes.cards.i;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.k.KuldothaRebirth;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItThatBetraysTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an opponent's sacrificed nontoken permanent under its control")
    void returnsOpponentSacrificedPermanent() {
        harness.addToBattlefield(player2, new ItThatBetrays());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, spellbook.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Spellbook");
        harness.assertNotInGraveyard(player1, "Spellbook");

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Does not trigger when its controller sacrifices a permanent")
    void doesNotTriggerForControllerSacrifice() {
        harness.addToBattlefield(player1, new ItThatBetrays());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, spellbook.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertNotOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("Does not trigger for a sacrificed token")
    void doesNotTriggerForToken() {
        harness.addToBattlefield(player2, new ItThatBetrays());
        Spellbook tokenCard = new Spellbook();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, token.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Spellbook");
    }
}
