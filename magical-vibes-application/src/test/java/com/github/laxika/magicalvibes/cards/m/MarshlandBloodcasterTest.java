package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarshlandBloodcaster.class, GrizzlyBears.class})
class MarshlandBloodcasterTest extends BaseCardTest {

    @Test
    void letsTheNextSpellPayLifeEqualToItsManaValue() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new MarshlandBloodcaster());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void permissionIsConsumedByTheNextSpell() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new MarshlandBloodcaster());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
