package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BurrGrafter;
import com.github.laxika.magicalvibes.cards.k.KodamasMight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HumbleBudoka.class, KodamasMight.class, BurrGrafter.class})
class HumbleBudokaTest extends BaseCardTest {

    @Test
    @DisplayName("Humble Budoka cannot be targeted by spells because it has shroud")
    void cannotBeTargetedBySpells() {
        Permanent budoka = harness.addToBattlefieldAndReturn(player2, new HumbleBudoka());
        harness.setHand(player1, List.of(new KodamasMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, budoka.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Humble Budoka cannot be targeted by abilities because it has shroud")
    void cannotBeTargetedByAbilities() {
        harness.addToBattlefield(player1, new BurrGrafter());
        Permanent budoka = harness.addToBattlefieldAndReturn(player2, new HumbleBudoka());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, budoka.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
