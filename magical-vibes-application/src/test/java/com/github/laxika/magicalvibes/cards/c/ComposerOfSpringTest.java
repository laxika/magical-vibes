package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ComposerOfSpring.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class})
class ComposerOfSpringTest extends BaseCardTest {

    @Test
    @DisplayName("Below six enchantments, Composer of Spring only puts a tapped land from hand")
    void belowSixEnchantmentsOnlyPutsLand() {
        harness.addToBattlefield(player1, new ComposerOfSpring());
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, new GloriousAnthem());
        castTriggeringEnchantment();
        harness.setHand(player1, List.of(forest, bears));

        resolveTrigger(true);
        harness.handleCardChosen(player1, 0);

        Permanent enteredLand = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == forest)
                .findFirst()
                .orElseThrow();
        assertThat(enteredLand.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("With six enchantments, Composer of Spring can put a creature from hand")
    void sixEnchantmentsAlsoPutsCreature() {
        harness.addToBattlefield(player1, new ComposerOfSpring());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new GloriousAnthem());
        }
        GrizzlyBears bears = new GrizzlyBears();
        castTriggeringEnchantment();
        harness.setHand(player1, List.of(bears));

        resolveTrigger(true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() == bears && permanent.isTapped());
    }

    @Test
    @DisplayName("Declining Composer of Spring's trigger leaves the hand unchanged")
    void decliningLeavesHandUnchanged() {
        harness.addToBattlefield(player1, new ComposerOfSpring());
        Forest forest = new Forest();
        castTriggeringEnchantment();
        harness.setHand(player1, List.of(forest));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == forest);
    }

    private void resolveTrigger(boolean accept) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accept);
        harness.passBothPriorities();
    }

    private void castTriggeringEnchantment() {
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }
}
