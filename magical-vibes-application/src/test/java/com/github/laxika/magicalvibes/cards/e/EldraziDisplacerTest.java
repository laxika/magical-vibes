package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EldraziDisplacer.class, GrizzlyBears.class})
class EldraziDisplacerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles another creature and returns it tapped under its owner's control")
    void flickersAnotherCreatureUnderItsOwnersControlTapped() {
        harness.addToBattlefield(player1, new EldraziDisplacer());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID oldBearsId = bears.getId();

        harness.activateAbility(player1, 0, null, oldBearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(oldBearsId);
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target Eldrazi Displacer itself")
    void cannotTargetItself() {
        harness.addToBattlefield(player1, new EldraziDisplacer());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID displacerId = harness.getPermanentId(player1, "Eldrazi Displacer");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, displacerId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires colorless mana in addition to two generic mana")
    void requiresColorlessMana() {
        harness.addToBattlefield(player1, new EldraziDisplacer());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
