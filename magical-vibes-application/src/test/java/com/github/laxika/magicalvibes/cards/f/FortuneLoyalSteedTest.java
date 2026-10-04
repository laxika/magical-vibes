package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.t.TrainedArynx;
import com.github.laxika.magicalvibes.cards.c.ColossalRattlewurm;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FortuneLoyalSteed.class, TrainedArynx.class, ColossalRattlewurm.class})
class FortuneLoyalSteedTest extends BaseCardTest {

    @Test
    @DisplayName("Fortune scries 2 when it enters")
    void entersAndScriesTwo() {
        harness.setHand(player1, List.of(new FortuneLoyalSteed()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

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
        Permanent helper = addCreatureReady(player1, new TrainedArynx());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(fortune.isSaddled()).isTrue();
        assertThat(helper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking while saddled flickers Fortune and the chosen saddler")
    void attacksAndFlickersFortuneAndSaddler() {
        Permanent fortune = addCreatureReady(player1, new FortuneLoyalSteed());
        Permanent helper = addCreatureReady(player1, new TrainedArynx());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        advanceToEndOfCombatChoice();

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

    @Test
    void mayDeclineToExileSaddler() {
        Permanent fortune = addCreatureReady(player1, new FortuneLoyalSteed());
        Permanent helper = addCreatureReady(player1, new TrainedArynx());

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        declareAttackers(List.of(0));
        resolveAllTriggers();
        advanceToEndOfCombatChoice();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .extracting(Permanent::getId).contains(helper.getId()).doesNotContain(fortune.getId());
        assertThat(helper.isTapped()).isTrue();
    }

    @Test
    void attackingWithoutSaddleDoesNotFlicker() {
        Permanent fortune = addCreatureReady(player1, new FortuneLoyalSteed());
        Permanent helper = addCreatureReady(player1, new TrainedArynx());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        resolveCombat();
        for (int i = 0; i < 8 && !gd.interaction.isAwaitingInput(); i++) {
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .extracting(Permanent::getId).containsExactlyInAnyOrder(fortune.getId(), helper.getId());
    }

    @Test
    void exiledTokenSaddlerDoesNotReturn() {
        Permanent fortune = addCreatureReady(player1, new FortuneLoyalSteed());
        TrainedArynx token = new TrainedArynx();
        token.setToken(true);
        Permanent helper = addCreatureReady(player1, token);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        declareAttackers(List.of(0));
        resolveAllTriggers();
        advanceToEndOfCombatChoice();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of(helper.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .extracting(Permanent::getId).doesNotContain(fortune.getId(), helper.getId());
    }

    @Test
    void stolenSaddlerReturnsToItsOwner() {
        addCreatureReady(player1, new FortuneLoyalSteed());
        Permanent helper = addCreatureReady(player1, new TrainedArynx());
        gd.stolenCreatures.put(helper.getId(), player2.getId());
        helper.getCard().setOwnerId(player2.getId());

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        declareAttackers(List.of(0));
        resolveAllTriggers();
        advanceToEndOfCombatChoice();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of(helper.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1)
                .extracting(Permanent::getCard).containsExactly(helper.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isTapped()).isFalse();
    }

    private void advanceToEndOfCombatChoice() {
        resolveCombat();
        for (int i = 0; i < 8 && !gd.interaction.isAwaitingInput(); i++) {
            harness.passBothPriorities();
        }
    }

    @Test
    void saddlerCanStillFlickerAfterFortuneDiesInCombat() {
        Permanent fortune = addCreatureReady(player1, new FortuneLoyalSteed());
        Permanent helper = addCreatureReady(player1, new TrainedArynx());
        addCreatureReady(player2, new ColossalRattlewurm());

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        declareAttackersAndPrepareBlockers(List.of(0));
        resolveAllTriggers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        advanceToEndOfCombatChoice();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fortune.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of(helper.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .extracting(Permanent::getCard).containsExactly(helper.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getId()).isNotEqualTo(helper.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fortune.getCard());
    }
}
