package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({ClawsOfValakut.class, GrizzlyBears.class, Mountain.class, FountainOfYouth.class})
class ClawsOfValakutTest extends BaseCardTest {

    @Test
    @DisplayName("Claws of Valakut gives the enchanted creature +1/+0 per Mountain and first strike")
    void resolvesAndBoostsPerMountain() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());

        harness.setHand(player1, List.of(new ClawsOfValakut()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Claws of Valakut updates dynamically when Mountain count changes")
    void updatesDynamicallyWithMountainCount() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent claws = harness.addToBattlefieldAndReturn(player1, new ClawsOfValakut());
        claws.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.addToBattlefield(player1, new Mountain());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.addToBattlefield(player1, new Mountain());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Mountain"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Claws of Valakut counts Mountains controlled by its controller")
    void countsAurasControllersMountains() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());

        Permanent claws = harness.addToBattlefieldAndReturn(player1, new ClawsOfValakut());
        claws.setAttachedTo(opponentBears.getId());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Claws of Valakut stops affecting the creature when it leaves the battlefield")
    void effectEndsWhenAuraLeavesBattlefield() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Mountain());

        Permanent claws = harness.addToBattlefieldAndReturn(player1, new ClawsOfValakut());
        claws.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(claws);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Claws of Valakut cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ClawsOfValakut()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Claws of Valakut fizzles if its target creature is removed before resolution")
    void fizzlesIfTargetRemovedBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClawsOfValakut()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Claws of Valakut");
        harness.assertNotOnBattlefield(player1, "Claws of Valakut");
    }

    @Test
    @DisplayName("Claws of Valakut grants first strike even without Mountains and only affects its host")
    void grantsFirstStrikeWithoutMountains() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClawsOfValakut()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FIRST_STRIKE)).isFalse();

        harness.addToBattlefield(player1, new Mountain());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Claws of Valakut stack their boosts and the remaining Aura keeps granting first strike")
    void multipleAurasStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        Permanent firstClaws = harness.addToBattlefieldAndReturn(player1, new ClawsOfValakut());
        firstClaws.setAttachedTo(bears.getId());
        Permanent secondClaws = harness.addToBattlefieldAndReturn(player1, new ClawsOfValakut());
        secondClaws.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(firstClaws);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }
}
