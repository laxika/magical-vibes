package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunesIntervention.class, Forest.class, GrizzlyBears.class, GloriousAnthem.class,
        HillGiant.class, Millstone.class, SolRing.class, Shock.class})
class SunesInterventionTest extends BaseCardTest {

    @Test
    void createsTwoKnightTokens() {
        cast(new int[]{0}, List.of());

        assertThat(findPermanents(player1, "Knight")).hasSize(2);
    }

    @Test
    void seeksNonlandPermanentWithManaValueAtMostThree() {
        harness.setLibrary(player1, List.of(new SolRing(), new Forest(), new HillGiant()));

        cast(new int[]{1}, List.of());

        harness.assertInHand(player1, "Sol Ring");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Forest", "Hill Giant");
    }

    @Test
    void destroysArtifactEnchantmentAndGainsLifeWhenAllTargetedModesAreChosen() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        int startingLife = gd.getLife(player2.getId());

        cast(new int[]{2, 3, 4}, List.of(artifact.getId(), enchantment.getId(), player2.getId()));

        harness.assertInGraveyard(player2, "Millstone");
        harness.assertInGraveyard(player2, "Glorious Anthem");
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife + 3);
    }

    @Test
    void artifactModeRejectsCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(new int[]{2}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resolvesAllFiveModesTogether() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setLife(player1, 10);

        cast(new int[]{0, 1, 2, 3, 4},
                List.of(artifact.getId(), enchantment.getId(), player1.getId()));

        assertThat(findPermanents(player1, "Knight")).hasSize(2).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        });
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Millstone");
        harness.assertInGraveyard(player2, "Glorious Anthem");
        harness.assertLife(player1, 13);
    }

    @Test
    void seeksManaValueThreeAndPreservesOrderOfOtherCards() {
        harness.setLibrary(player1, List.of(new Forest(), new GloriousAnthem(), new HillGiant()));

        cast(new int[]{1}, List.of());

        harness.assertInHand(player1, "Glorious Anthem");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Hill Giant");
    }

    @Test
    void seekWithNoMatchStillResolvesOtherModes() {
        harness.setLibrary(player1, List.of(new Forest(), new HillGiant(), new Shock()));
        harness.setLife(player1, 10);

        cast(new int[]{1, 4}, List.of(player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Hill Giant", "Shock");
        harness.assertLife(player1, 13);
    }

    @Test
    void seekFromEmptyLibraryStillCreatesTokens() {
        harness.setLibrary(player1, List.of());

        cast(new int[]{0, 1}, List.of());

        assertThat(findPermanents(player1, "Knight")).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void enchantmentModeRejectsArtifactTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());

        assertThatThrownBy(() -> cast(new int[]{3}, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    void allTargetsIllegalPreventsEvenUntargetedModes() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        prepareCast();
        harness.castModalInstantWithModes(player1, 0, 1, 5,
                new int[]{0, 1, 2}, List.of(artifact.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(artifact);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Knight")).isEmpty();
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Sune's Intervention");
    }

    @Test
    void oneIllegalTargetDoesNotPreventOtherModes() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setLife(player1, 10);
        prepareCast();
        harness.castModalInstantWithModes(player1, 0, 1, 5,
                new int[]{0, 2, 3, 4},
                List.of(artifact.getId(), enchantment.getId(), player1.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(artifact);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Knight")).hasSize(2);
        harness.assertInGraveyard(player2, "Glorious Anthem");
        harness.assertLife(player1, 13);
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds) {
        prepareCast();
        harness.castModalInstantWithModes(player1, 0, 1, 5, modes, targetIds);
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new SunesIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
