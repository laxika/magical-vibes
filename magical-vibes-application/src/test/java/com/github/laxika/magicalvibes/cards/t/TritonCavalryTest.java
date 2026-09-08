package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TritonCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("A spell targeting Triton Cavalry prompts to return an enchantment")
    void targetingCavalryPromptsForEnchantmentBounce() {
        Permanent cavalry = addCavalry();
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        castTargetingSpell(cavalry);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, anthem.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Glorious Anthem");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Declining the heroic ability leaves the enchantment on the battlefield")
    void decliningHeroicLeavesEnchantment() {
        Permanent cavalry = addCavalry();
        harness.addToBattlefield(player2, new GloriousAnthem());
        castTargetingSpell(cavalry);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Glorious Anthem");
        harness.assertNotInHand(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("The heroic ability only allows enchantment targets")
    void heroicAbilityOnlyTargetsEnchantments() {
        Permanent cavalry = addCavalry();
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        castTargetingSpell(cavalry);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(anthem.getId())
                .doesNotContain(bears.getId());
        harness.handlePermanentChosen(player1, anthem.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A spell that does not target Triton Cavalry does not trigger heroic")
    void spellTargetingPlayerDoesNotTriggerHeroic() {
        addCavalry();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private Permanent addCavalry() {
        return harness.addToBattlefieldAndReturn(player1, new TritonCavalry());
    }

    private void castTargetingSpell(Permanent cavalry) {
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, cavalry.getId());
    }
}
