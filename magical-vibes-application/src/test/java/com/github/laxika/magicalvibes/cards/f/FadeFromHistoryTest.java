package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FadeFromHistoryTest extends BaseCardTest {

    @Test
    @DisplayName("Each player who controls an artifact or enchantment creates a Bear before those permanents are destroyed")
    void createsBearForEachEligiblePlayerThenDestroysArtifactsAndEnchantments() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new RuleOfLaw());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castFadeFromHistory();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Rule of Law");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bearCount(player1.getId())).isEqualTo(1);
        assertThat(bearCount(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("A player without an artifact or enchantment creates no Bear")
    void playerWithoutArtifactOrEnchantmentCreatesNoBear() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castFadeFromHistory();

        assertThat(bearCount(player1.getId())).isEqualTo(1);
        assertThat(bearCount(player2.getId())).isZero();
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private long bearCount(UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Bear"))
                .count();
    }

    private void castFadeFromHistory() {
        harness.setHand(player1, List.of(new FadeFromHistory()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
