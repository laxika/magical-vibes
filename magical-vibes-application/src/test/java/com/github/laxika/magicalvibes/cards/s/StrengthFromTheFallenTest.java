package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FesteringGoblin;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StrengthFromTheFallenTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry boosts the chosen creature by the number of creature cards in its graveyard")
    void ownEntryBoostsTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new FesteringGoblin(), new GloriousAnthem()));
        harness.setHand(player1, List.of(new StrengthFromTheFallen()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent target = gqs.findPermanentById(gd, bears.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Another enchantment entering under your control triggers the boost")
    void allyEnchantmentEntryBoostsTarget() {
        harness.addToBattlefield(player1, new StrengthFromTheFallen());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new FesteringGoblin()));
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(basePower + 3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(baseToughness + 3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.setHand(player1, List.of(new StrengthFromTheFallen()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, anthem.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
