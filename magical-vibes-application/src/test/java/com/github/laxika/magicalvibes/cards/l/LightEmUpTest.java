package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightEmUp.class, AirElemental.class, GrizzlyBears.class, Island.class})
class LightEmUpTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a creature")
    void dealsDamageToCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new LightEmUp()));
        addMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Casualty copies the damage spell")
    void casualtyCopiesSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent casualtyCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightEmUp()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), casualtyCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(casualtyCreature.getId()));
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.setHand(player1, List.of(new LightEmUp()));
        addMana();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
