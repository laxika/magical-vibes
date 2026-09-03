package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreamCoat.class, GrizzlyBears.class})
class DreamCoatTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature becomes the chosen colors indefinitely")
    void enchantedCreatureBecomesChosenColorsIndefinitely() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DreamCoat()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Dream Coat");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectiveColors(gd, creature))
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.BLUE);

        gd.expireEndOfTurnFloatingEffects();
        creature.resetModifiers();
        assertThat(gqs.getEffectiveColors(gd, creature))
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.BLUE);
    }

    @Test
    @DisplayName("Dream Coat can be activated only once each turn")
    void activatesOnlyOnceEachTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DreamCoat()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        int auraIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Dream Coat"));
        harness.activateAbility(player1, auraIndex, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "DONE");

        assertThatThrownBy(() -> harness.activateAbility(player1, auraIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
