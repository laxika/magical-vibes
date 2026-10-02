package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OmenOfTheSea;
import com.github.laxika.magicalvibes.cards.w.WillowElf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CelebornTheWise.class, OmenOfTheSea.class, WillowElf.class, GrizzlyBears.class})
class CelebornTheWiseTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with an Elf scries 1 and boosts Celeborn by the looked-at count")
    void elfAttackScriesAndBoostsCeleborn() {
        Permanent celeborn = addCreatureReady(player1, new CelebornTheWise());
        Permanent elf = addCreatureReady(player1, new WillowElf());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(elf)));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, celeborn)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, celeborn)).isEqualTo(4);
    }

    @Test
    @DisplayName("Celeborn gets +1/+1 for each card looked at by a larger scry")
    void largerScryUsesCardsLookedAt() {
        Permanent celeborn = addCreatureReady(player1, new CelebornTheWise());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new OmenOfTheSea()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, celeborn)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, celeborn)).isEqualTo(5);
    }

    @Test
    @DisplayName("Attacking without an Elf does not trigger Celeborn's scry")
    void nonElfAttackDoesNotScry() {
        Permanent celeborn = addCreatureReady(player1, new CelebornTheWise());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears)));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gqs.getEffectivePower(gd, celeborn)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, celeborn)).isEqualTo(3);
    }
}
