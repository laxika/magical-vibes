package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VesselOfEphemera.class})
class VesselOfEphemeraTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing the Vessel creates two white 1/1 flying Spirit tokens")
    void createsTwoFlyingSpiritTokens() {
        harness.addToBattlefield(player1, new VesselOfEphemera());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vessel of Ephemera");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Spirit"))
                .hasSize(2)
                .allSatisfy(permanent -> assertSpiritToken(permanent));
    }

    @Test
    @DisplayName("Sacrifice is paid immediately, before the tokens are created")
    void sacrificesBeforeResolution() {
        harness.addToBattlefield(player1, new VesselOfEphemera());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Vessel of Ephemera");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2)
                .allSatisfy(this::assertSpiritToken);
    }

    @Test
    @DisplayName("Activation requires white mana and does not sacrifice on failed payment")
    void cannotActivateWithoutWhiteMana() {
        harness.addToBattlefield(player1, new VesselOfEphemera());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Vessel of Ephemera");
        harness.assertNotInGraveyard(player1, "Vessel of Ephemera");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Vessel can be activated on the opponent's turn")
    void nonactiveControllerCanActivateTappedVessel() {
        Permanent vessel = harness.addToBattlefieldAndReturn(player2, new VesselOfEphemera());
        vessel.tap();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Vessel of Ephemera");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .hasSize(2)
                .allSatisfy(this::assertSpiritToken);
    }

    private void assertSpiritToken(Permanent permanent) {
        assertThat(permanent.getCard().isToken()).isTrue();
        assertThat(permanent.isTapped()).isFalse();
        assertThat(permanent.getCard().getPower()).isEqualTo(1);
        assertThat(permanent.getCard().getToughness()).isEqualTo(1);
        assertThat(permanent.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(permanent.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.FLYING)).isTrue();
    }
}
