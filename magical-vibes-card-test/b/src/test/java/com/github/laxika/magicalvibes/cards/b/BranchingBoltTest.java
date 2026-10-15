package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.c.CloudheathDrake;
import com.github.laxika.magicalvibes.cards.j.JungleWeaver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BranchingBolt.class, GrizzlyBears.class, SuntailHawk.class, CloudheathDrake.class, JungleWeaver.class})
class BranchingBoltTest extends BaseCardTest {

    private void giveMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Mode 0 deals 3 damage to a creature with flying, killing it")
    void mode0KillsFlyingCreature() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new BranchingBolt()));
        giveMana(player1);

        harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "Suntail Hawk"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Mode 0 cannot target a creature without flying")
    void mode0CannotTargetNonFlyingCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SuntailHawk()); // valid target so spell is castable
        harness.setHand(player1, List.of(new BranchingBolt()));
        giveMana(player1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode 1 deals 3 damage to a creature without flying, killing it")
    void mode1KillsNonFlyingCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BranchingBolt()));
        giveMana(player1);

        harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Mode 1 cannot target a creature with flying")
    void mode1CannotTargetFlyingCreature() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addToBattlefield(player2, new GrizzlyBears()); // valid target so spell is castable
        harness.setHand(player1, List.of(new BranchingBolt()));
        giveMana(player1);

        UUID hawkId = harness.getPermanentId(player2, "Suntail Hawk");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, hawkId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode 2 deals 3 damage to both a flying and a non-flying creature, killing both")
    void mode2KillsBothCreatures() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BranchingBolt()));
        giveMana(player1);

        UUID hawkId = harness.getPermanentId(player2, "Suntail Hawk");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castModalInstant(player1, 0, 2, List.of(hawkId, bearsId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Suntail Hawk");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Branching Bolt goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new BranchingBolt()));
        giveMana(player1);

        harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "Suntail Hawk"));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Branching Bolt");
    }

    @Test
    @DisplayName("Choosing both deals exactly 3 damage to each target")
    void bothModesDealExactlyThreeDamage() {
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new CloudheathDrake());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new JungleWeaver());
        harness.setHand(player1, List.of(new BranchingBolt()));
        giveMana(player1);

        harness.castModalInstant(player1, 0, 2, List.of(drake.getId(), spider.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Cloudheath Drake");
        harness.assertOnBattlefield(player2, "Jungle Weaver");
        assertThat(spider.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Choosing both still damages the second target when the first gains hexproof")
    void bothModesResolveWithFirstTargetIllegal() {
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new CloudheathDrake());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new JungleWeaver());
        harness.setHand(player1, List.of(new BranchingBolt()));
        giveMana(player1);

        harness.castModalInstant(player1, 0, 2, List.of(drake.getId(), spider.getId()));
        drake.getGrantedKeywords().add(Keyword.HEXPROOF);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Cloudheath Drake");
        assertThat(drake.getMarkedDamage()).isZero();
        assertThat(spider.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Branching Bolt");
    }

    @Test
    @DisplayName("Choosing both skips a nonflying target that gains flying")
    void bothModesResolveWithSecondTargetIllegal() {
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new CloudheathDrake());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new JungleWeaver());
        harness.setHand(player1, List.of(new BranchingBolt()));
        giveMana(player1);

        harness.castModalInstant(player1, 0, 2, List.of(drake.getId(), spider.getId()));
        spider.getGrantedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Cloudheath Drake");
        harness.assertOnBattlefield(player2, "Jungle Weaver");
        assertThat(spider.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The nonflying mode does not resolve if its only target gains flying")
    void onlyTargetGainingFlyingPreventsResolution() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new JungleWeaver());
        harness.setHand(player1, List.of(new BranchingBolt()));
        giveMana(player1);

        harness.castInstant(player1, 0, 1, spider.getId());
        spider.getGrantedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Jungle Weaver");
        assertThat(spider.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Branching Bolt");
    }
}
