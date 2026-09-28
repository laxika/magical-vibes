package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NaturalAffinity.class, Forest.class, Mountain.class, AirElemental.class, ImprisonedInTheMoon.class})
class NaturalAffinityTest extends BaseCardTest {

    @Test
    @DisplayName("Animates lands of both players as 2/2 creatures, still lands")
    void animatesAllLands() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        forest.setSummoningSick(false);

        harness.castFromHand(player1, new NaturalAffinity(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(2);
        assertThat(gqs.isLand(gd, mountain)).isTrue();
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
}
