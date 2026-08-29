package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloatedToadTest extends BaseCardTest {

    @Test
    @DisplayName("Bloated Toad has protection from blue")
    void hasProtectionFromBlue() {
        Permanent toad = new Permanent(new BloatedToad());
        gd.playerBattlefields.get(player1.getId()).add(toad);

        assertThat(gqs.hasProtectionFrom(gd, toad, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, toad, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Cycling discards Bloated Toad and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new BloatedToad()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Bloated Toad");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
