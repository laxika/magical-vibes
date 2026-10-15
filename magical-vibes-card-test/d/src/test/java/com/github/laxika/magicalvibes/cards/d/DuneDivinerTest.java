package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SurvivorsEncampment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DuneDiviner.class, SurvivorsEncampment.class})
class DuneDivinerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} and tapping a Desert gains 1 life")
    void activateGainsLifeAndTapsDesert() {
        Permanent diviner = harness.addToBattlefieldAndReturn(player1, new DuneDiviner());
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new SurvivorsEncampment());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player1.getId());
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(diviner);
        harness.activateAbility(player1, idx, null, null);
        assertThat(desert.isTapped()).isTrue();
        harness.assertLife(player1, lifeBefore);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 1);
        assertThat(desert.isTapped()).isTrue();
        assertThat(diviner.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without an untapped Desert")
    void cannotActivateWithoutUntappedDesert() {
        Permanent diviner = harness.addToBattlefieldAndReturn(player1, new DuneDiviner());
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new SurvivorsEncampment());
        desert.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(diviner);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a Desert")
    void cannotActivateWithoutDesert() {
        Permanent diviner = harness.addToBattlefieldAndReturn(player1, new DuneDiviner());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(diviner);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Dune Diviner can activate")
    void tappedSummoningSickDivinerCanActivate() {
        Permanent diviner = harness.addToBattlefieldAndReturn(player1, new DuneDiviner());
        diviner.tap();
        diviner.setSummoningSick(true);
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new SurvivorsEncampment());
        harness.addMana(player1, ManaColor.GREEN, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 1);
        assertThat(diviner.isTapped()).isTrue();
        assertThat(desert.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Desert cannot pay the cost")
    void cannotTapOpponentsDesert() {
        harness.addToBattlefield(player1, new DuneDiviner());
        Permanent desert = harness.addToBattlefieldAndReturn(player2, new SurvivorsEncampment());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(desert.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The mana cost must be paid in addition to tapping a Desert")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new DuneDiviner());
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new SurvivorsEncampment());
        int lifeBefore = gd.getLife(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(desert.isTapped()).isFalse();
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Only the chosen Desert is tapped when multiple Deserts are available")
    void choosesOneDesertAndCannotReuseIt() {
        Permanent diviner = harness.addToBattlefieldAndReturn(player1, new DuneDiviner());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SurvivorsEncampment());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SurvivorsEncampment());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, second.getId());
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isTrue();
        harness.assertLife(player1, lifeBefore);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 2);
        assertThat(first.isTapped()).isTrue();
        assertThat(diviner.isTapped()).isFalse();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
