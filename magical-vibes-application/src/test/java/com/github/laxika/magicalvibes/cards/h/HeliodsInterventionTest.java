package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FlickerOfFate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeliodsIntervention.class, Ornithopter.class, GloriousAnthem.class, GrizzlyBears.class,
        FlickerOfFate.class})
class HeliodsInterventionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroy mode destroys exactly X target artifacts and/or enchantments")
    void destroysExactlyXArtifactsAndEnchantments() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HeliodsIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playModalXCard(gd, player1, 0, 0, 2, null, List.of(artifact.getId(), enchantment.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroy mode requires exactly X targets")
    void destroyModeRequiresExactlyXTargets() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new HeliodsIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> gs.playModalXCard(
                gd, player1, 0, 0, 2, null, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroy mode rejects a nonartifact nonenchantment permanent")
    void destroyModeRejectsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HeliodsIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> gs.playModalXCard(
                gd, player1, 0, 0, 1, null, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Life mode makes the target player gain twice X life")
    void targetPlayerGainsTwiceXLife() {
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new HeliodsIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castModalInstantForX(player1, 0, 1, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void destroyModeCanBeCastWithZeroXAndNoTargets() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new HeliodsIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castModalInstantForX(player1, 0, 0, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player1, "Heliod's Intervention");
    }

    @Test
    void lifeModeCanTargetItsController() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 12);
        harness.setHand(player1, List.of(new HeliodsIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castModalInstantForX(player1, 0, 1, 3, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 12);
    }

    @Test
    void lifeModeWithZeroXGainsNoLife() {
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new HeliodsIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castModalInstantForX(player1, 0, 1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
        harness.assertInGraveyard(player1, "Heliod's Intervention");
    }

    @Test
    void destroyModeRejectsDuplicateTargets() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new HeliodsIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> gs.playModalXCard(
                gd, player1, 0, 0, 2, null, List.of(artifact.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroyModeCanDestroyMoreThanOneHundredTargets() {
        List<UUID> targets = IntStream.range(0, 101)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new Ornithopter()).getId())
                .toList();
        harness.setHand(player1, List.of(new HeliodsIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 103);

        gs.playModalXCard(gd, player1, 0, 0, 101, null, targets);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(101);
    }

    @Test
    void destroyModeDestroysRemainingLegalTargetAfterAnotherTargetIsBlinked() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new HeliodsIntervention()));
        harness.setHand(player2, List.of(new FlickerOfFate()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player2, ManaColor.WHITE, 2);

        gs.playModalXCard(gd, player1, 0, 0, 2, null, List.of(artifact.getId(), enchantment.getId()));
        harness.castInstant(player2, 0, artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Heliod's Intervention");
    }
}
