package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FortuneLoyalSteed.class, GrizzlyBears.class})
class FortuneLoyalSteedTest extends BaseCardTest {

    @Test
    @DisplayName("Fortune scries 2 when it enters")
    void entersAndScriesTwo() {
        harness.setHand(player1, List.of(new FortuneLoyalSteed()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
    }

    @Test
    @DisplayName("Saddling Fortune records the creature that paid the cost")
    void saddleTapsAnotherCreatureAndSaddlesFortune() {
        Permanent fortune = addCreatureReady(player1, new FortuneLoyalSteed());
        Permanent helper = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(fortune.isSaddled()).isTrue();
        assertThat(helper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking while saddled flickers Fortune and the chosen saddler")
    void attacksAndFlickersFortuneAndSaddler() {
        Permanent fortune = addCreatureReady(player1, new FortuneLoyalSteed());
        Permanent helper = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        for (int i = 0; i < 8 && !gd.interaction.isAwaitingInput(); i++) {
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of(helper.getId()));

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        if (scry != null) {
            harness.getGameService().handleInteractionAnswer(
                    gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        }

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2);
        assertThat(battlefield).extracting(Permanent::getId)
                .doesNotContain(fortune.getId(), helper.getId());
    }
}
