package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrimocEscapee.class, FugitiveWizard.class})
class PrimocEscapeeTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new PrimocEscapee()));
        harness.setLibrary(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Primoc Escapee");
        harness.assertInHand(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Cycling cannot be activated without paying {2}")
    void cyclingRequiresTwoGenericMana() {
        harness.setHand(player1, List.of(new PrimocEscapee()));
        harness.setLibrary(player1, List.of(new FugitiveWizard()));

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertInHand(player1, "Primoc Escapee");
        harness.assertNotInGraveyard(player1, "Primoc Escapee");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Primoc Escapee")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent attacker = addCreatureReady(player1, new PrimocEscapee());
        Permanent blocker = addCreatureReady(player2, new FugitiveWizard());

        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }
}
