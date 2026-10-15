package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MomentaryBlink;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({ThundercloudElemental.class, AirElemental.class, GrizzlyBears.class, MomentaryBlink.class})
class ThundercloudElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Taps creatures with toughness 2 or less on either battlefield")
    void tapsSmallCreatures() {
        Permanent elemental = addCreatureReady(player1, new ThundercloudElemental());
        Permanent ownSmallCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentSmallCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent opponentLargeCreature = addCreatureReady(player2, new AirElemental());

        activateAbility(0);

        assertThat(elemental.isTapped()).isFalse();
        assertThat(ownSmallCreature.isTapped()).isTrue();
        assertThat(opponentSmallCreature.isTapped()).isTrue();
        assertThat(opponentLargeCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Makes all other creatures lose flying until end of turn")
    void removesFlyingFromOtherCreatures() {
        Permanent elemental = addCreatureReady(player1, new ThundercloudElemental());
        Permanent ownFlyer = addCreatureReady(player1, new AirElemental());
        Permanent opponentFlyer = addCreatureReady(player2, new AirElemental());

        activateAbility(1);

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownFlyer, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentFlyer, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownFlyer, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentFlyer, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not affect creatures that enter after the ability resolves")
    void doesNotAffectCreaturesEnteringLater() {
        addCreatureReady(player1, new ThundercloudElemental());

        activateAbility(1);

        Permanent laterFlyer = addCreatureReady(player2, new AirElemental());

        assertThat(gqs.hasKeyword(gd, laterFlyer, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Only the activating Elemental is excluded from losing flying")
    void removesFlyingFromAnotherThundercloudElemental() {
        Permanent source = addCreatureReady(player1, new ThundercloudElemental());
        Permanent other = addCreatureReady(player2, new ThundercloudElemental());

        activateAbility(1);

        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The tap ability checks modified toughness when it resolves")
    void checksToughnessAtResolution() {
        addCreatureReady(player1, new ThundercloudElemental());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);

        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent laterBear = addCreatureReady(player2, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isFalse();
        assertThat(laterBear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A source that leaves and returns before resolution also loses flying")
    void returnedSourceIsAnotherCreature() {
        Permanent source = addCreatureReady(player1, new ThundercloudElemental());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 1, null, null);

        harness.setHand(player1, List.of(new MomentaryBlink()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, source.getId());
        Permanent returned = findPermanent(player1, "Thundercloud Elemental");
        assertThat(returned.getId()).isNotEqualTo(source.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isFalse();
    }

    private void activateAbility(int abilityIndex) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.passBothPriorities();
    }
}
