package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BoneMask;
import com.github.laxika.magicalvibes.cards.d.DwarvenMiner;
import com.github.laxika.magicalvibes.cards.l.LionsEyeDiamond;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinTinkerer.class, BoneMask.class, LionsEyeDiamond.class, DwarvenMiner.class})
class GoblinTinkererTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact and takes damage equal to its mana value")
    void destroysArtifactAndTakesManaValueDamage() {
        Permanent tinkerer = addCreatureReady(player1, new GoblinTinkerer());
        harness.addToBattlefield(player2, new BoneMask()); // {4}, mana value 4
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Bone Mask");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bone Mask");
        // 4 damage to a 1/2 is lethal
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tinkerer);
        harness.assertInGraveyard(player1, "Goblin Tinkerer");
    }

    @Test
    @DisplayName("Survives when the destroyed artifact's mana value is 0")
    void survivesZeroManaValueArtifact() {
        Permanent tinkerer = addCreatureReady(player1, new GoblinTinkerer());
        harness.addToBattlefield(player2, new LionsEyeDiamond()); // {0}, mana value 0
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Lion's Eye Diamond");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Lion's Eye Diamond");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tinkerer);
        assertThat(tinkerer.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        addCreatureReady(player1, new GoblinTinkerer());
        harness.addToBattlefield(player2, new DwarvenMiner());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Dwarven Miner");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
