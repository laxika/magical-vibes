package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AshnodsTransmogrant;
import com.github.laxika.magicalvibes.cards.d.DartingMerfolk;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.i.IronLance;
import com.github.laxika.magicalvibes.cards.i.IvoryMask;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FountainWatch.class, IronLance.class, IvoryMask.class, DartingMerfolk.class,
        Disenchant.class, AshnodsTransmogrant.class})
class FountainWatchTest extends BaseCardTest {

    @Test
    @DisplayName("Artifacts and enchantments you control have shroud")
    void artifactsAndEnchantmentsYouControlHaveShroud() {
        harness.addToBattlefield(player1, new FountainWatch());
        Permanent ironLance = harness.addToBattlefieldAndReturn(player1, new IronLance());
        Permanent ivoryMask = harness.addToBattlefieldAndReturn(player1, new IvoryMask());

        assertThat(gqs.hasKeyword(gd, ironLance, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, ivoryMask, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Creatures without artifact or enchantment types are unaffected")
    void otherPermanentsAreUnaffected() {
        harness.addToBattlefield(player1, new FountainWatch());
        Permanent dartingMerfolk = harness.addToBattlefieldAndReturn(player1, new DartingMerfolk());
        Permanent opponentIronLance = harness.addToBattlefieldAndReturn(player2, new IronLance());

        assertThat(gqs.hasKeyword(gd, dartingMerfolk, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentIronLance, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Granted shroud is lost when Fountain Watch leaves the battlefield")
    void shroudIsLostWhenFountainWatchLeaves() {
        Permanent fountainWatch = harness.addToBattlefieldAndReturn(player1, new FountainWatch());
        Permanent ironLance = harness.addToBattlefieldAndReturn(player1, new IronLance());

        assertThat(gqs.hasKeyword(gd, ironLance, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(fountainWatch);

        assertThat(gqs.hasKeyword(gd, ironLance, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Neither player can target protected artifacts or enchantments")
    void shroudPreventsBothPlayersFromTargeting() {
        harness.addToBattlefield(player1, new FountainWatch());
        Permanent ironLance = harness.addToBattlefieldAndReturn(player1, new IronLance());
        Permanent ivoryMask = harness.addToBattlefieldAndReturn(player1, new IvoryMask());

        for (var player : List.of(player1, player2)) {
            harness.setHand(player, List.of(new Disenchant()));
            harness.addMana(player, ManaColor.WHITE, 1);
            harness.addMana(player, ManaColor.COLORLESS, 1);
            for (var target : List.of(ironLance, ivoryMask)) {
                assertThatThrownBy(() -> harness.castInstant(player, 0, target.getId()))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("shroud");
            }
        }
    }

    @Test
    @DisplayName("Opponent's enchantments remain targetable")
    void opponentsEnchantmentRemainsTargetable() {
        harness.addToBattlefield(player1, new FountainWatch());
        Permanent ivoryMask = harness.addToBattlefieldAndReturn(player2, new IvoryMask());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, ivoryMask.getId());

        harness.assertNotOnBattlefield(player2, "Ivory Mask");
        harness.assertInGraveyard(player2, "Ivory Mask");
    }

    @Test
    @DisplayName("Gaining shroud before resolution prevents a targeted spell from resolving")
    void gainingShroudBeforeResolutionProtectsArtifact() {
        Permanent ironLance = harness.addToBattlefieldAndReturn(player1, new IronLance());
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, ironLance.getId());
        harness.enterBattlefieldAndReturn(player1, new FountainWatch());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Iron Lance");
        harness.assertInGraveyard(player2, "Disenchant");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fountain Watch gains its own shroud when it becomes an artifact")
    void artifactFountainWatchProtectsItself() {
        Permanent fountainWatch = harness.addToBattlefieldAndReturn(player1, new FountainWatch());
        harness.addToBattlefield(player1, new AshnodsTransmogrant());

        harness.activateAbility(player1, 1, null, fountainWatch.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, fountainWatch, Keyword.SHROUD)).isTrue();
    }
}
