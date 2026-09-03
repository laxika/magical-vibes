package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NovaCleric;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProfanePrayers.class, NovaCleric.class, GrizzlyBears.class, Plains.class})
class ProfanePrayersTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage and gains life equal to Clerics on the battlefield")
    void countsClericsOnAllBattlefields() {
        harness.addToBattlefield(player1, new NovaCleric());
        harness.addToBattlefield(player2, new NovaCleric());
        harness.addToBattlefield(player2, new NovaCleric());
        harness.setHand(player1, List.of(new ProfanePrayers()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Counts Clerics at resolution")
    void countsClericsAtResolution() {
        harness.addToBattlefield(player1, new NovaCleric());
        harness.addToBattlefield(player1, new NovaCleric());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ProfanePrayers()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Nova Cleric"));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        var plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new ProfanePrayers()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
