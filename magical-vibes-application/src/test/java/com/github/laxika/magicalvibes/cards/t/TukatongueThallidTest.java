package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TukatongueThallidTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Tukatongue Thallid puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new TukatongueThallid()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Tukatongue Thallid"));
    }

    // ===== Death trigger =====

    @Test
    @DisplayName("When Tukatongue Thallid dies, a Saproling token is created")
    void deathTriggerCreatesToken() {
        harness.addToBattlefield(player1, new TukatongueThallid());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath — Tukatongue Thallid dies

        GameData gd = harness.getGameData();

        // Tukatongue Thallid should be in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tukatongue Thallid"));

        // One death trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve the death trigger
        harness.passBothPriorities();

        // A Saproling token should be on the battlefield
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Saproling"))
                .toList();
        assertThat(tokens).hasSize(1);
    }
}
