package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(EnvoyOfOkinecAhau.class)
class EnvoyOfOkinecAhauTest extends BaseCardTest {

    @Test
    void createsGnomeTokenWithoutTapping() {
        Permanent envoy = harness.addToBattlefieldAndReturn(player1, new EnvoyOfOkinecAhau());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Gnome");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.GNOME);
        assertThat(envoy.isTapped()).isFalse();
    }

    @Test
    void canActivateRepeatedlyWhileTapped() {
        Permanent envoy = harness.addToBattlefieldAndReturn(player1, new EnvoyOfOkinecAhau());
        envoy.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().getSubtypes()).contains(CardSubtype.GNOME);
                    assertThat(token.isTapped()).isFalse();
                });
        assertThat(envoy.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void cannotActivateWithoutWhiteMana() {
        harness.addToBattlefield(player1, new EnvoyOfOkinecAhau());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }
}
