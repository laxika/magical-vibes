package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BattlewandOak;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.r.Rootgrapple;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimberProtector.class, BattlewandOak.class, Forest.class, GrizzlyBears.class,
        WrathOfGod.class, Lignify.class, Rootgrapple.class})
class TimberProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Other Treefolk creatures you control get +1/+1 and indestructible")
    void buffsOtherTreefolk() {
        Permanent oak = harness.addToBattlefieldAndReturn(player1, new BattlewandOak());
        int basePower = gqs.getEffectivePower(gd, oak);
        int baseToughness = gqs.getEffectiveToughness(gd, oak);

        harness.addToBattlefield(player1, new TimberProtector());

        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(baseToughness + 1);
        assertThat(gqs.hasKeyword(gd, oak, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Forests you control have indestructible but get no +1/+1")
    void grantsIndestructibleToForests() {
        harness.addToBattlefield(player1, new TimberProtector());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Timber Protector does not buff or protect itself")
    void doesNotAffectItself() {
        Permanent protector = harness.addToBattlefieldAndReturn(player1, new TimberProtector());
        // "Other Treefolk" excludes the source.
        assertThat(gqs.hasKeyword(gd, protector, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Does not affect non-Treefolk creatures")
    void doesNotAffectNonTreefolk() {
        harness.addToBattlefield(player1, new TimberProtector());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Does not affect an opponent's Treefolk or Forests")
    void doesNotAffectOpponentPermanents() {
        harness.addToBattlefield(player1, new TimberProtector());
        Permanent opponentOak = harness.addToBattlefieldAndReturn(player2, new BattlewandOak());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        assertThat(gqs.hasKeyword(gd, opponentOak, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentForest, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Protected Treefolk survives Wrath of God while Timber Protector dies")
    void protectedTreefolkSurvivesWrath() {
        harness.addToBattlefield(player1, new TimberProtector());
        harness.addToBattlefield(player1, new BattlewandOak());

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, 0);

        // Battlewand Oak is indestructible from the Protector and survives.
        harness.assertOnBattlefield(player1, "Battlewand Oak");
        // Timber Protector does not protect itself and is destroyed.
        harness.assertNotOnBattlefield(player1, "Timber Protector");
    }

    @Test
    @DisplayName("Two Timber Protectors boost and protect each other against a board wipe")
    void twoProtectorsProtectEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TimberProtector());
        int power = gqs.getEffectivePower(gd, first);
        int toughness = gqs.getEffectiveToughness(gd, first);
        assertThat(power).isEqualTo(4);
        assertThat(toughness).isEqualTo(6);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TimberProtector());
        Permanent oak = harness.addToBattlefieldAndReturn(player1, new BattlewandOak());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(power + 1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(toughness + 1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(power + 1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(toughness + 1);
        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(5);

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanents(player1, "Timber Protector")).hasSize(2);
        harness.assertOnBattlefield(player1, "Battlewand Oak");
    }

    @Test
    @DisplayName("Protection and the boost end when Timber Protector leaves the battlefield")
    void bonusesEndWhenProtectorDies() {
        harness.addToBattlefield(player1, new TimberProtector());
        Permanent oak = harness.addToBattlefieldAndReturn(player1, new BattlewandOak());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new WrathOfGod(), new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Battlewand Oak");
        harness.assertInGraveyard(player1, "Timber Protector");
        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, oak, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.assertInGraveyard(player1, "Battlewand Oak");
    }

    @Test
    @DisplayName("A protected Forest survives a destruction spell")
    void forestSurvivesDestruction() {
        harness.addToBattlefield(player1, new TimberProtector());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Rootgrapple()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, forest.getId());

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("A noncreature Treefolk Aura you control is protected even on an opposing creature")
    void protectsNoncreatureTreefolk() {
        harness.addToBattlefield(player1, new TimberProtector());
        Permanent oak = harness.addToBattlefieldAndReturn(player2, new BattlewandOak());
        harness.setHand(player1, List.of(new Lignify(), new Rootgrapple()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castEnchantment(player1, 0, oak.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Lignify");

        assertThat(gqs.hasKeyword(gd, aura, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, oak, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, oak)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(4);

        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.assertOnBattlefield(player1, "Lignify");
        harness.assertNotInGraveyard(player1, "Lignify");
    }
}
