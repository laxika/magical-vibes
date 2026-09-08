package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaskedBandits.class, Island.class})
class MaskedBanditsTest extends BaseCardTest {

    @Test
    void handAbilityExilesTheCardAndGrantsOnlyBlackRedOrGreenMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        MaskedBandits bandits = new MaskedBandits();
        harness.setHand(player1, List.of(bandits));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, land.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(bandits.getId())).isNotNull();

        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("BLACK", "RED", "GREEN");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    void landGrantEndsWhenMaskedBanditsIsCastFromExile() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        MaskedBandits bandits = new MaskedBandits();
        harness.setHand(player1, List.of(bandits));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");
        land.untap();

        harness.castFromExile(player1, bandits.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
