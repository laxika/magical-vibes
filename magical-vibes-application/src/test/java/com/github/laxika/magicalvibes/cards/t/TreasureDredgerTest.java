package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TreasureDredger.class)
class TreasureDredgerTest extends BaseCardTest {

    @Test
    @DisplayName("{1}, {T}, Pay 1 life creates a Treasure token")
    void createsTreasureToken() {
        Permanent dredger = harness.addToBattlefieldAndReturn(player1, new TreasureDredger());
        dredger.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(dredger.isTapped()).isTrue();
        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.getCard().isToken()).isTrue();
        assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
    }

    @Test
    @DisplayName("Treasure Dredger cannot activate without {1}")
    void cannotActivateWithoutMana() {
        Permanent dredger = harness.addToBattlefieldAndReturn(player1, new TreasureDredger());
        dredger.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
