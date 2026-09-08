package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HoldoutSettlement.class, GrizzlyBears.class})
class HoldoutSettlementTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C}")
    void tapAddsColorlessMana() {
        harness.addToBattlefield(player1, new HoldoutSettlement());
        Permanent land = gd.playerBattlefields.get(player1.getId()).get(0);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{T}, Tap an untapped creature: Add one mana of any color")
    void tapsCreatureAndAddsChosenColorMana() {
        harness.addToBattlefield(player1, new HoldoutSettlement());
        Permanent land = gd.playerBattlefields.get(player1.getId()).get(0);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate creature-tap ability without an untapped creature")
    void cannotActivateWithoutUntappedCreature() {
        harness.addToBattlefield(player1, new HoldoutSettlement());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
