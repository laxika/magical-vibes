package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.ArixmethesSlumberingIsle;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GerrardsIrregulars;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NaturalAffinity.class, Forest.class, Mountain.class, GrizzlyBears.class, GerrardsIrregulars.class, ImprisonedInTheMoon.class})
class NaturalAffinityTest extends BaseCardTest {

    private void cast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new NaturalAffinity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
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
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.isAnimatedUntilEndOfTurn()).isFalse();
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
    @CardUsed(ArixmethesSlumberingIsle.class)
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
    @DisplayName("Animation wears off at end of turn (resetModifiers)")
    void animationWearsOff() {
        harness.addToBattlefield(player1, new Forest());

        cast();

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isAnimatedUntilEndOfTurn()).isTrue();

        gd.expireEndOfTurnFloatingEffects();
        forest.resetModifiers();

        assertThat(forest.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.getEffectivePower(gd, forest)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, forest)).isZero();
    }

    @Test
    @DisplayName("Animates a non-land card that is currently a land")
    void animatesPermanentThatBecameLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GerrardsIrregulars());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new ImprisonedInTheMoon());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.isCreature(gd, creature)).isFalse();
        assertThat(gqs.isLand(gd, creature)).isTrue();

        harness.setHand(player1, List.of(new NaturalAffinity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.isCreature(gd, creature)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.isLand(gd, creature)).isTrue();
    }
}
