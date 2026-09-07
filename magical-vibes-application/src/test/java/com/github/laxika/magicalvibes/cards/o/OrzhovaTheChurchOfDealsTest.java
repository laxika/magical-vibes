package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrzhovaTheChurchOfDeals.class, Forest.class})
class OrzhovaTheChurchOfDealsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapAbilityAddsColorlessMana() {
        Permanent orzhova = harness.addToBattlefieldAndReturn(player1, new OrzhovaTheChurchOfDeals());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(orzhova.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Pays mana to make a player lose life and gain 1 life")
    void lifeAbilityDrainsTargetPlayer() {
        Permanent orzhova = harness.addToBattlefieldAndReturn(player1, new OrzhovaTheChurchOfDeals());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(orzhova.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Rejects a permanent as the target")
    void lifeAbilityRequiresAPlayerTarget() {
        harness.addToBattlefield(player1, new OrzhovaTheChurchOfDeals());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player");
    }
}
