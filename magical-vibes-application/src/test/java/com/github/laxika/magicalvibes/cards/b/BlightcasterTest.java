package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BlightcasterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an enchantment lets the controller give target creature -2/-2")
    void enchantmentCastShrinksTargetCreature() {
        harness.addToBattlefield(player1, new Blightcaster());
        harness.addToBattlefield(player2, new HillGiant());
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, giantId);
        harness.passBothPriorities(); // resolve the triggered ability
        harness.handleMayAbilityChosen(player1, true);

        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the may choice leaves the target unchanged")
    void decliningLeavesTargetUnchanged() {
        harness.addToBattlefield(player1, new Blightcaster());
        harness.addToBattlefield(player2, new HillGiant());
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, giantId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature reduced to 0 toughness dies")
    void lethalShrinkKillsCreature() {
        harness.addToBattlefield(player1, new Blightcaster());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> "Grizzly Bears".equals(p.getCard().getName()));
    }

    @Test
    @DisplayName("Casting a non-enchantment spell does not trigger the ability")
    void nonEnchantmentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Blightcaster());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

}
