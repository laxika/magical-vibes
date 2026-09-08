package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BanishingLight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShimmerwingChimera.class, BanishingLight.class, GrizzlyBears.class})
class ShimmerwingChimeraTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to one other enchantment you control during your upkeep")
    void returnsSelectedEnchantment() {
        Permanent chimera = harness.addToBattlefieldAndReturn(player1, new ShimmerwingChimera());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new BanishingLight());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new BanishingLight());

        advanceToUpkeep(player1);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(enchantment.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(enchantment.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .doesNotContain(enchantment.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .contains(chimera.getId());
        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment.getCard());
    }

    @Test
    @DisplayName("Can decline returning an enchantment")
    void canDeclineTarget() {
        Permanent chimera = harness.addToBattlefieldAndReturn(player1, new ShimmerwingChimera());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new BanishingLight());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class))
                .isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(chimera.getId(), enchantment.getId());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(enchantment.getCard());
    }
}
