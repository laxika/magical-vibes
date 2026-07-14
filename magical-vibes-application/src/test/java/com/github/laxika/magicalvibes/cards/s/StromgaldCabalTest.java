package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StromgaldCabalTest extends BaseCardTest {

    // ===== Counters a white spell, paying 1 life =====

    @Test
    @DisplayName("Counters target white spell and pays 1 life")
    void countersWhiteSpell() {
        StromgaldCabal cabal = new StromgaldCabal();
        addCreatureReady(player1, cabal);
        harness.setLife(player1, 20);

        EliteVanguard victim = new EliteVanguard();
        harness.setHand(player2, List.of(victim));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, victim.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Elite Vanguard is countered — goes to graveyard, 1 life paid
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Elite Vanguard"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elite Vanguard"));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    // ===== Cannot target a non-white spell =====

    @Test
    @DisplayName("Cannot target a green spell")
    void cannotTargetGreenSpell() {
        StromgaldCabal cabal = new StromgaldCabal();
        addCreatureReady(player1, cabal);
        harness.setLife(player1, 20);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
