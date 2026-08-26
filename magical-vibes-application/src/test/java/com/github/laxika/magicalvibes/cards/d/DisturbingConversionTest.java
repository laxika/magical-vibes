package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisturbingConversion.class, Forest.class, GrizzlyBears.class, Opt.class})
class DisturbingConversionTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, each player mills two cards")
    void eachPlayerMillsTwoCards() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int player1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int player2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        castAura(player1, bears.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckBefore - 2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckBefore - 2);
    }

    @Test
    @DisplayName("Enchanted creature gets -1/-0 for each card in its controller's graveyard")
    void debuffUsesEnchantedCreatureControllersGraveyard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Opt(), new Opt(), new Opt()));
        harness.setGraveyard(player2, List.of(new Opt()));

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DisturbingConversion());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Debuff updates when the enchanted creature controller's graveyard changes")
    void debuffUpdatesDynamically() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DisturbingConversion());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(new Opt(), new Opt()));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new DisturbingConversion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castAura(com.github.laxika.magicalvibes.model.Player player, java.util.UUID targetId) {
        harness.setHand(player, List.of(new DisturbingConversion()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
