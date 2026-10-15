package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AxebaneBeast;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SavageSmash.class, GrizzlyBears.class, LlanowarElves.class,
        SauroformHybrid.class, AxebaneBeast.class})
class SavageSmashTest extends BaseCardTest {

    @Test
    @DisplayName("The boost applies before the fight")
    void boostAppliesBeforeFight() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new SavageSmash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveSorcery(player1, 0, List.of(bearId, elvesId));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Llanowar Elves");

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new SavageSmash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveSorcery(player1, 0, List.of(bearId, elvesId));

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature as the first target")
    void cannotTargetOpponentCreatureFirst() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new SavageSmash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID theirBearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID theirElvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(theirBearId, theirElvesId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target your own creature as the second target")
    void cannotTargetOwnCreatureSecond() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new SavageSmash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearId, elvesId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boosted power kills a creature that would survive the unboosted fight")
    void boostedPowerIsUsedForFightDamage() {
        harness.addToBattlefield(player1, new SauroformHybrid());
        harness.addToBattlefield(player2, new AxebaneBeast());
        prepareSmash();

        Permanent hybrid = findPermanent(player1, "Sauroform Hybrid");
        UUID beastId = harness.getPermanentId(player2, "Axebane Beast");
        harness.castAndResolveSorcery(player1, 0, List.of(hybrid.getId(), beastId));

        harness.assertOnBattlefield(player1, "Sauroform Hybrid");
        harness.assertInGraveyard(player2, "Axebane Beast");
        assertThat(hybrid.getMarkedDamage()).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, hybrid)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hybrid)).isEqualTo(4);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The boost still applies when the opposing target leaves before resolution")
    void opposingTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new SauroformHybrid());
        harness.addToBattlefield(player2, new AxebaneBeast());
        prepareSmash();

        Permanent hybrid = findPermanent(player1, "Sauroform Hybrid");
        UUID beastId = harness.getPermanentId(player2, "Axebane Beast");
        harness.castSorcery(player1, 0, List.of(hybrid.getId(), beastId));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hybrid)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hybrid)).isEqualTo(4);
        assertThat(hybrid.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Savage Smash");
    }

    @Test
    @DisplayName("Neither creature fights when the first target changes controller")
    void firstTargetChangesControllerBeforeResolution() {
        harness.addToBattlefield(player1, new SauroformHybrid());
        harness.addToBattlefield(player2, new AxebaneBeast());
        prepareSmash();

        Permanent hybrid = findPermanent(player1, "Sauroform Hybrid");
        Permanent beast = findPermanent(player2, "Axebane Beast");
        harness.castSorcery(player1, 0, List.of(hybrid.getId(), beast.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(hybrid);
        gd.playerBattlefields.get(player2.getId()).add(hybrid);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sauroform Hybrid");
        harness.assertOnBattlefield(player2, "Axebane Beast");
        assertThat(gqs.getEffectivePower(gd, hybrid)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hybrid)).isEqualTo(2);
        assertThat(hybrid.getMarkedDamage()).isZero();
        assertThat(beast.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Savage Smash");
    }

    @Test
    @DisplayName("Only the boost applies when the opposing target changes controller")
    void secondTargetChangesControllerBeforeResolution() {
        harness.addToBattlefield(player1, new SauroformHybrid());
        harness.addToBattlefield(player2, new AxebaneBeast());
        prepareSmash();

        Permanent hybrid = findPermanent(player1, "Sauroform Hybrid");
        Permanent beast = findPermanent(player2, "Axebane Beast");
        harness.castSorcery(player1, 0, List.of(hybrid.getId(), beast.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(beast);
        gd.playerBattlefields.get(player1.getId()).add(beast);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sauroform Hybrid");
        harness.assertOnBattlefield(player1, "Axebane Beast");
        assertThat(gqs.getEffectivePower(gd, hybrid)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hybrid)).isEqualTo(4);
        assertThat(hybrid.getMarkedDamage()).isZero();
        assertThat(beast.getMarkedDamage()).isZero();
    }

    private void prepareSmash() {
        harness.setHand(player1, List.of(new SavageSmash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
