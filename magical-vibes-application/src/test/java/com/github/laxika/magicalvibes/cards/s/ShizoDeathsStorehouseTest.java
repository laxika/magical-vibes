package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShizoDeathsStorehouse.class, IsamaruHoundOfKonda.class, WanderingOnes.class})
class ShizoDeathsStorehouseTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds {B}")
    void manaAbilityAddsBlack() {
        Permanent shizo = harness.addToBattlefieldAndReturn(player1, new ShizoDeathsStorehouse());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(shizo.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Grants fear to a legendary creature")
    void grantsFearToLegendaryCreature() {
        Permanent shizo = harness.addToBattlefieldAndReturn(player1, new ShizoDeathsStorehouse());
        Permanent isamaru = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, isamaru.getId());
        harness.passBothPriorities();

        assertThat(shizo.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gqs.hasKeyword(gd, isamaru, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Fear wears off at end of turn")
    void fearWearsOff() {
        Permanent isamaru = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        harness.addToBattlefield(player1, new ShizoDeathsStorehouse());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 1, 1, null, isamaru.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, isamaru, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("A nonlegendary creature is not a legal target")
    void nonlegendaryCreatureIsIllegalTarget() {
        Permanent shizo = harness.addToBattlefieldAndReturn(player1, new ShizoDeathsStorehouse());
        Permanent wanderingOnes = harness.addToBattlefieldAndReturn(player1, new WanderingOnes());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, wanderingOnes.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shizo.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("A legendary noncreature is not a legal target")
    void legendaryNoncreatureIsIllegalTarget() {
        Permanent shizo = harness.addToBattlefieldAndReturn(player1, new ShizoDeathsStorehouse());
        Permanent otherShizo = harness.addToBattlefieldAndReturn(player1, new ShizoDeathsStorehouse());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, otherShizo.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shizo.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's legendary creature can be targeted")
    void opponentLegendaryCreatureCanBeTargeted() {
        Permanent shizo = harness.addToBattlefieldAndReturn(player1, new ShizoDeathsStorehouse());
        Permanent isamaru = harness.addToBattlefieldAndReturn(player2, new IsamaruHoundOfKonda());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, isamaru.getId());
        harness.passBothPriorities();

        assertThat(shizo.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, isamaru, Keyword.FEAR)).isTrue();
    }
}
