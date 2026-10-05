package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.ArixmethesSlumberingIsle;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NaturalAffinity.class, Forest.class, Mountain.class, AirElemental.class,
        ImprisonedInTheMoon.class, ArixmethesSlumberingIsle.class, TreetopVillage.class})
class NaturalAffinityTest extends BaseCardTest {

    private void cast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new NaturalAffinity(), "{2}{G}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Animates lands of both players as 2/2 creatures, still lands")
    void animatesAllLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        cast();

        Permanent ownForest = findPermanent(player1, "Forest");
        assertThat(ownForest.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(ownForest.getEffectivePower()).isEqualTo(2);
        assertThat(ownForest.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.isCreature(gd, ownForest)).isTrue();
        assertThat(gqs.isLand(gd, ownForest)).isTrue();

        Permanent opponentMountain = findPermanent(player2, "Mountain");
        assertThat(opponentMountain.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(opponentMountain.getEffectivePower()).isEqualTo(2);
        assertThat(opponentMountain.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.isCreature(gd, opponentMountain)).isTrue();
        assertThat(gqs.isLand(gd, opponentMountain)).isTrue();
    }

    @Test
    @DisplayName("Does not animate non-land permanents")
    void doesNotAnimateNonLands() {
        harness.addToBattlefield(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.castFromHand(player1, new NaturalAffinity(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, creature)).isTrue();
        assertThat(gqs.isLand(gd, creature)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Animates a non-land card that is currently a land")
    void animatesPermanentThatBecameLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new ImprisonedInTheMoon());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.isCreature(gd, creature)).isFalse();
        assertThat(gqs.isLand(gd, creature)).isTrue();

        harness.castFromHand(player1, new NaturalAffinity(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, creature)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.isLand(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("Does not animate a land that enters after resolution")
    void doesNotAnimateLandEnteringAfterResolution() {
        harness.castFromHand(player1, new NaturalAffinity(), "{2}{G}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Mountain()));
        harness.playLand(player1, 0);

        Permanent mountain = findPermanent(player1, "Mountain");
        assertThat(gqs.isCreature(gd, mountain)).isFalse();
        assertThat(gqs.isLand(gd, mountain)).isTrue();
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOff() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.castFromHand(player1, new NaturalAffinity(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, forest)).isZero();
    }

    @Test
    @DisplayName("Does not animate lands entering after resolution")
    void doesNotAnimateLandsEnteringAfterResolution() {
        harness.addToBattlefield(player1, new Forest());

        cast();
        Permanent laterForest = harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(laterForest.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, laterForest)).isFalse();
        assertThat(gqs.isLand(gd, laterForest)).isTrue();
    }

    @Test
    @DisplayName("Animates permanents that are currently lands even when their cards are not lands")
    void animatesCurrentLands() {
        Permanent arixmethes = harness.enterBattlefieldAndReturn(player2,
                new ArixmethesSlumberingIsle());

        assertThat(gqs.isLand(gd, arixmethes)).isTrue();
        assertThat(gqs.isCreature(gd, arixmethes)).isFalse();

        cast();

        assertThat(arixmethes.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, arixmethes)).isTrue();
        assertThat(gqs.getEffectivePower(gd, arixmethes)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, arixmethes)).isEqualTo(2);
        assertThat(gqs.isLand(gd, arixmethes)).isTrue();
    }

    @Test
    @DisplayName("Preserves an animated land's green color while setting its base stats to 2/2")
    void preservesEarlierAnimationColor() {
        Permanent village = animateVillage();

        cast();

        assertThat(gqs.getEffectivePower(gd, village)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, village)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, village)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Preserves an animated land's existing creature subtype")
    void preservesEarlierAnimationSubtype() {
        Permanent village = animateVillage();

        cast();

        assertThat(gqs.isCreature(gd, village)).isTrue();
        assertThat(gqs.isLand(gd, village)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, village, CardSubtype.APE)).isTrue();
    }

    private Permanent animateVillage() {
        Permanent village = harness.addToBattlefieldAndReturn(player1, new TreetopVillage());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, village)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, village)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasEffectiveSubtype(gd, village, CardSubtype.APE)).isTrue();
        return village;
    }
}
