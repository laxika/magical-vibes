package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Grapeshot.class, GrizzlyBears.class})
class GrapeshotTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target player")
    void dealsDamageToPlayer() {
        harness.setLife(player2, 20);
        castGrapeshot(player2.getId());

        resolveSpellAndStorm();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to target creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castGrapeshot(targetId);

        resolveSpellAndStorm();

        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Grapeshot")
    void stormCreatesCopiesForEachPriorSpell() {
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());
        castGrapeshot(player2.getId());

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    private void castGrapeshot(UUID targetId) {
        harness.setHand(player1, List.of(new Grapeshot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, targetId);
    }

    private void resolveSpellAndStorm() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
