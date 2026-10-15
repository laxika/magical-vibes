package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DayOfJudgment;
import com.github.laxika.magicalvibes.cards.d.DiamondPickAxe;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.OrazcaPuzzleDoor;
import com.github.laxika.magicalvibes.cards.q.QuicksandWhirlpool;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KitesailLarcenist.class, LlanowarElves.class, GrizzlyBears.class, DayOfJudgment.class,
        DiamondPickAxe.class, OrazcaPuzzleDoor.class, QuicksandWhirlpool.class})
class KitesailLarcenistTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms up to one artifact or creature per player and grants the Treasure ability")
    void transformsOnePermanentPerPlayerAndGrantsTreasureAbility() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castLarcenist(List.of(ownCreature.getId(), opposingCreature.getId()));

        assertTreasure(ownCreature);
        assertTreasure(opposingCreature);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Allows at most one chosen permanent per player")
    void allowsAtMostOnePermanentPerPlayer() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareLarcenistCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    @Test
    @DisplayName("The transformation ends when Kitesail Larcenist leaves the battlefield")
    void transformationEndsWhenSourceLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castLarcenist(List.of(target.getId()));
        assertTreasure(target);

        harness.castFromHand(player1, new DayOfJudgment(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kitesail Larcenist");
        assertThat(target.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(target.getCard().hasType(CardType.ARTIFACT)).isFalse();
    }

    @Test
    void artifactLosesItsPreviousSubtypeAndIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DiamondPickAxe());

        castLarcenist(List.of(target.getId()));

        assertTreasure(target);
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.EQUIPMENT)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void opposingPlayerCanSacrificeTransformedArtifactForMana() {
        harness.setHand(player2, List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrazcaPuzzleDoor());
        castLarcenist(List.of(target.getId()));

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "BLUE");

        harness.assertInGraveyard(player2, "Orazca Puzzle-Door");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void sourceLeavingBeforeTriggerResolvesPreventsTransformation() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrazcaPuzzleDoor());
        prepareLarcenistCast();
        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new QuicksandWhirlpool()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Kitesail Larcenist"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kitesail Larcenist");
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.TREASURE)).isFalse();
        assertThat(gqs.hasLostPrintedAbilities(gd, target)).isFalse();
    }

    @Test
    void illegalTargetDoesNotPreventOtherTargetFromBecomingTreasure() {
        harness.setLibrary(player2, List.of());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new OrazcaPuzzleDoor());
        Permanent opposingTarget = harness.addToBattlefieldAndReturn(player2, new OrazcaPuzzleDoor());
        prepareLarcenistCast();
        harness.castCreature(player1, 0, List.of(ownTarget.getId(), opposingTarget.getId()));
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Orazca Puzzle-Door");
        assertTreasure(ownTarget);
    }

    @Test
    void canChooseNoTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DiamondPickAxe());

        castLarcenist(List.of());

        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.TREASURE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertOnBattlefield(player1, "Kitesail Larcenist");
    }

    private void castLarcenist(List<UUID> targetIds) {
        prepareLarcenistCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareLarcenistCast() {
        harness.setHand(player1, List.of(new KitesailLarcenist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void assertTreasure(Permanent permanent) {
        assertThat(gqs.isArtifact(gd, permanent)).isTrue();
        assertThat(gqs.isCreature(gd, permanent)).isFalse();
        assertThat(gqs.computeStaticBonus(gd, permanent).grantedSubtypes()).contains(CardSubtype.TREASURE);
    }
}
