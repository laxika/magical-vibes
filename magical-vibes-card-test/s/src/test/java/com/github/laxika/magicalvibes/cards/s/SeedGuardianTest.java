package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeedGuardian.class, GrizzlyBears.class, Forest.class, WrathOfGod.class})
class SeedGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("When Seed Guardian dies, it creates an Elemental sized to creature cards in its graveyard")
    void deathCreatesElementalSizedToCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.addToBattlefield(player1, new SeedGuardian());

        destroyAllCreatures();

        Permanent elemental = findPermanent(player1, "Elemental");
        assertThat(elemental.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(elemental.getCard().getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(elemental.getEffectivePower()).isEqualTo(2);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Seed Guardian counts only creature cards in its controller's graveyard")
    void deathIgnoresNonCreatureAndOpponentGraveyardCards() {
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new SeedGuardian());

        destroyAllCreatures();

        Permanent elemental = findPermanent(player1, "Elemental");
        assertThat(elemental.getEffectivePower()).isEqualTo(1);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Seed Guardian counts creatures that die at the same time")
    void countsSimultaneouslyDyingCreatures() {
        harness.addToBattlefield(player1, new SeedGuardian());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyAllCreatures();

        Permanent elemental = findPermanent(player1, "Elemental");
        assertThat(elemental.getEffectivePower()).isEqualTo(2);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(elemental);
        harness.assertNotOnBattlefield(player2, "Elemental");
    }

    @Test
    @DisplayName("The graveyard is counted at resolution and the token's size is then fixed")
    void countsAtResolutionAndDoesNotContinuouslyUpdateTokenSize() {
        harness.addToBattlefield(player1, new SeedGuardian());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Seed Guardian");
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, List.of(new SeedGuardian(), new SeedGuardian(), new Forest()));
        harness.passBothPriorities();

        Permanent elemental = findPermanent(player1, "Elemental");
        assertThat(elemental.getEffectivePower()).isEqualTo(2);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(2);

        harness.setGraveyard(player1, List.of());
        assertThat(elemental.getEffectivePower()).isEqualTo(2);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("An empty graveyard at resolution produces a token that dies immediately")
    void zeroToughnessTokenDoesNotSurvive() {
        harness.addToBattlefield(player1, new SeedGuardian());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Seed Guardian");
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elemental");
        assertThat(gd.stack).isEmpty();
    }

    private void destroyAllCreatures() {
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
