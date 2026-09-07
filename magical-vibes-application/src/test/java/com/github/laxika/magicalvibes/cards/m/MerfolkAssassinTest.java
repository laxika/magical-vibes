package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.cards.w.WarBarge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MerfolkAssassin.class, Squire.class, WarBarge.class})
class MerfolkAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature with islandwalk")
    void destroysTargetCreatureWithIslandwalk() {
        Permanent assassin = addCreatureReady(player1, new MerfolkAssassin());
        Permanent barge = harness.addToBattlefieldAndReturn(player1, new WarBarge());
        Permanent target = addCreatureReady(player2, new Squire());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(barge), 0, null,
                target.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(assassin), null,
                target.getId());
        assertThat(assassin.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Squire");
    }

    @Test
    @DisplayName("Cannot target a creature without islandwalk")
    void cannotTargetCreatureWithoutIslandwalk() {
        addCreatureReady(player1, new MerfolkAssassin());
        Permanent target = addCreatureReady(player2, new Squire());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with islandwalk");
    }
}
