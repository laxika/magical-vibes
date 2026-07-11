package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ImpracticalJokeTest extends BaseCardTest {

    // ===== Damage + prevention flag =====

    @Test
    @DisplayName("Deals 3 to target creature and makes damage unpreventable this turn")
    void deals3AndSetsUnpreventable() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.setHand(player1, List.of(new ImpracticalJoke()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, List.of(targetId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Hill Giant (3/3) takes 3 damage → dead
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        // Damage can't be prevented this turn
        assertThat(gd.damageCantBePreventedThisTurn).isTrue();
        assertThat(gqs.isDamagePreventable(gd)).isFalse();
    }

    // ===== No target =====

    @Test
    @DisplayName("Can be cast with no target; still makes damage unpreventable this turn")
    void castWithNoTargetSetsUnpreventable() {
        harness.setHand(player1, List.of(new ImpracticalJoke()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.damageCantBePreventedThisTurn).isTrue();
        harness.assertInGraveyard(player1, "Impractical Joke");
    }

    // ===== Does not kill a bigger creature =====

    @Test
    @DisplayName("Deals exactly 3 to a surviving creature")
    void deals3ToSurvivor() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        UUID targetId = harness.getPermanentId(player2, "Avatar of Might");
        harness.setHand(player1, List.of(new ImpracticalJoke()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, List.of(targetId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Avatar of Might");
        assertThat(avatar.getMarkedDamage()).isEqualTo(3);
    }
}
