package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuneBroodNephilim.class, Forest.class, GrizzlyBears.class})
class DuneBroodNephilimTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage creates one colorless Sand token for each land you control")
    void combatDamageCreatesSandTokensForEachLandYouControl() {
        Permanent nephilim = addCreatureReady(player1, new DuneBroodNephilim());
        nephilim.setAttacking(true);
        addLands(player1, 3);
        addLands(player2, 2);

        resolveCombat();
        harness.passBothPriorities();

        List<Permanent> tokens = sandTokens(player1);
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isNull();
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SAND);
        });
    }

    @Test
    @DisplayName("A blocked Dune-Brood Nephilim does not create Sand tokens")
    void blockedCombatDamageCreatesNoSandTokens() {
        Permanent nephilim = addCreatureReady(player1, new DuneBroodNephilim());
        nephilim.setAttacking(true);
        addLands(player1, 3);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(sandTokens(player1)).isEmpty();
    }

    private void addLands(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }

    private List<Permanent> sandTokens(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.SAND))
                .toList();
    }
}
