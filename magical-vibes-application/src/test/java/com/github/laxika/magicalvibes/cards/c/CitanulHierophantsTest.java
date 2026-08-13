package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CitanulHierophantsTest extends BaseCardTest {

    @Test
    @DisplayName("Citanul Hierophants and other creatures you control can tap for green mana")
    void grantsGreenManaAbilityToControlledCreaturesIncludingItself() {
        Permanent hierophants = harness.addToBattlefieldAndReturn(player1, new CitanulHierophants());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        hierophants.setSummoningSick(false);
        bears.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Citanul Hierophants does not grant the ability to creatures an opponent controls")
    void doesNotGrantAbilityToOpponentCreatures() {
        harness.addToBattlefield(player1, new CitanulHierophants());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentBears.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
