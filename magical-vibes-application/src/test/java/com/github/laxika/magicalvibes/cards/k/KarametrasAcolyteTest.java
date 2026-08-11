package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.ElvishArchdruid;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KarametrasAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability adds green mana equal to green devotion")
    void tapAbilityAddsGreenManaEqualToDevotion() {
        harness.addToBattlefield(player1, new KarametrasAcolyte());
        harness.addToBattlefield(player1, new ElvishArchdruid());
        harness.addToBattlefield(player1, new LlanowarElves());

        Permanent acolyte = findPermanent(player1, "Karametra's Acolyte");
        acolyte.setSummoningSick(false);

        int acolyteIndex = gd.playerBattlefields.get(player1.getId()).indexOf(acolyte);
        harness.activateAbility(player1, acolyteIndex, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
    }

    @Test
    @DisplayName("Devotion ignores permanents without green mana symbols and opponents' permanents")
    void tapAbilityIgnoresNonGreenAndOpponentsPermanents() {
        harness.addToBattlefield(player1, new KarametrasAcolyte());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new ElvishArchdruid());

        Permanent acolyte = findPermanent(player1, "Karametra's Acolyte");
        acolyte.setSummoningSick(false);

        int acolyteIndex = gd.playerBattlefields.get(player1.getId()).indexOf(acolyte);
        harness.activateAbility(player1, acolyteIndex, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability cannot be activated with summoning sickness")
    void tapAbilityBlockedBySummoningSickness() {
        harness.addToBattlefield(player1, new KarametrasAcolyte());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
