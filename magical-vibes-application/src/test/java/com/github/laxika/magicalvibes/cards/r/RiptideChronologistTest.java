package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiptideChronologist.class, AvianChangeling.class, GrizzlyBears.class, HillGiant.class})
class RiptideChronologistTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and untaps every creature of the chosen type")
    void sacrificesAndUntapsChosenType() {
        Permanent chronologist = addCreatureReady(player1, new RiptideChronologist());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        ownBear.tap();
        opponentBear.tap();
        giant.tap();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(chronologist);
        assertThat(ownBear.isTapped()).isFalse();
        assertThat(opponentBear.isTapped()).isFalse();
        assertThat(giant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A Changeling is untapped as a creature of the chosen type")
    void changelingMatchesChosenType() {
        addCreatureReady(player1, new RiptideChronologist());
        Permanent changeling = addCreatureReady(player2, new AvianChangeling());
        changeling.tap();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(changeling.isTapped()).isFalse();
    }
}
