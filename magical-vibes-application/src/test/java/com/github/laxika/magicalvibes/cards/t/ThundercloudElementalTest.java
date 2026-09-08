package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThundercloudElemental.class, AirElemental.class, GrizzlyBears.class})
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

    private void activateAbility(int abilityIndex) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.passBothPriorities();
    }
}
