package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VolcanicEruptionTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 destroys two Mountains and deals 2 damage to each creature and each player")
    void destroysMountainsAndBlasts() {
        Permanent m1 = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent m2 = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2 dies to 2 damage
        harness.addToBattlefield(player1, new HillGiant());     // 3/3 survives 2 damage
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new VolcanicEruption()));
        harness.addMana(player1, ManaColor.BLUE, 5); // X=2: {2}{U}{U}{U}

        harness.castSorcery(player1, 0, 2, List.of(m1.getId(), m2.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mountain");
        harness.assertLife(player1, 18); // caster is also "each player"
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Damage equals only the Mountains actually put into a graveyard this way")
    void damageCountsActuallyDestroyed() {
        Permanent m1 = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent m2 = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new VolcanicEruption()));
        harness.addMana(player1, ManaColor.BLUE, 5); // X=2

        harness.castSorcery(player1, 0, 2, List.of(m1.getId(), m2.getId()));

        // One targeted Mountain leaves before resolution — only one is put into a graveyard,
        // so the blast deals 1 damage, not 2.
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(m2.getId()));

        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("X=0 destroys nothing and deals no damage")
    void xZeroDoesNothing() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new VolcanicEruption()));
        harness.addMana(player1, ManaColor.BLUE, 3); // X=0: {U}{U}{U}

        harness.castSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a non-Mountain permanent")
    void cannotTargetNonMountain() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new VolcanicEruption()));
        harness.addMana(player1, ManaColor.BLUE, 4); // X=1

        UUID forestId = forest.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(forestId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Mountains");
    }
}
