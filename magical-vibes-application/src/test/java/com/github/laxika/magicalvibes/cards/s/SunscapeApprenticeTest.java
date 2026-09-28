package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunscapeApprentice.class, RagingKavu.class})
class SunscapeApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("{G}, {T}: target creature gets +1/+1 until end of turn")
    void boostsTargetCreature() {
        Permanent apprentice = addReadyApprentice();
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new RagingKavu());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, kavu.getId());
        harness.passBothPriorities();

        assertThat(apprentice.isTapped()).isTrue();
        assertThat(kavu.getPowerModifier()).isEqualTo(1);
        assertThat(kavu.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("{G}, {T}: can target a creature an opponent controls")
    void boostsOpponentsCreature() {
        Permanent apprentice = addReadyApprentice();
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new RagingKavu());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, kavu.getId());
        harness.passBothPriorities();

        assertThat(apprentice.isTapped()).isTrue();
        assertThat(kavu.getPowerModifier()).isEqualTo(1);
        assertThat(kavu.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("{U}, {T}: puts target creature you control on top of its owner's library")
    void tucksControlledCreature() {
        Permanent apprentice = addReadyApprentice();
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new RagingKavu());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, kavu.getId());
        harness.passBothPriorities();

        assertThat(apprentice.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(kavu.getId()));
        assertThat(gd.playerDecks.get(player1.getId()).get(0).getName())
                .isEqualTo("Raging Kavu");
    }

    @Test
    @DisplayName("The tuck ability cannot target a creature an opponent controls")
    void tuckRejectsOpponentCreature() {
        addReadyApprentice();
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new RagingKavu());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, kavu.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    private Permanent addReadyApprentice() {
        return addCreatureReady(player1, new SunscapeApprentice());
    }
}
