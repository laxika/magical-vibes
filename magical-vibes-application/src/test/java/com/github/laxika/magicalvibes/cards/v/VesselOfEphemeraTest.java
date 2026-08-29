package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void assertSpiritToken(Permanent permanent) {
        assertThat(permanent.getCard().getPower()).isEqualTo(1);
        assertThat(permanent.getCard().getToughness()).isEqualTo(1);
        assertThat(permanent.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(permanent.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.FLYING)).isTrue();
    }
}
