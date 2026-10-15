package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RamblingPossum.class})
class RamblingPossumTest extends BaseCardTest {

    @Test
    @DisplayName("A saddled attack boosts Rambling Possum and may return its saddler")
    void saddledAttackBoostsAndReturnsSaddler() {
        Permanent possum = addCreatureReady(player1, new RamblingPossum());
        Permanent saddler = addCreatureReady(player1, new RamblingPossum());
        Permanent unrelated = addCreatureReady(player1, new RamblingPossum());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, saddler.getId());
        harness.passBothPriorities();
        assertThat(possum.isSaddled()).isTrue();
        assertThat(saddler.isTapped()).isTrue();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, possum)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, possum)).isEqualTo(5);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(saddler.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(saddler.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(possum, unrelated);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(saddler.getCard().getId()));

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gqs.getEffectivePower(gd, possum)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, possum)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the optional return leaves the saddler on the battlefield")
    void mayDeclineReturn() {
        Permanent possum = addCreatureReady(player1, new RamblingPossum());
        Permanent saddler = addCreatureReady(player1, new RamblingPossum());

        saddle(possum);
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(possum, saddler);
        assertThat(gqs.getEffectivePower(gd, possum)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, possum)).isEqualTo(5);
    }

    @Test
    @DisplayName("Attacking while not saddled does not trigger Rambling Possum")
    void notSaddledDoesNotTrigger() {
        Permanent possum = addCreatureReady(player1, new RamblingPossum());
        Permanent saddler = addCreatureReady(player1, new RamblingPossum());

        declareAttackers(player1, List.of(0));

        assertThat(gqs.getEffectivePower(gd, possum)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, possum)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saddler);
    }

    @Test
    @DisplayName("Accepting the return permits choosing zero creatures")
    void mayChooseZeroCreatures() {
        Permanent possum = addCreatureReady(player1, new RamblingPossum());
        Permanent saddler = addCreatureReady(player1, new RamblingPossum());

        saddle(possum);
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(possum, saddler);
        assertThat(gqs.getEffectivePower(gd, possum)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, possum)).isEqualTo(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Creatures from separate saddle activations are eligible and a subset may be returned")
    void mayReturnSubsetOfCreaturesFromMultipleSaddleActivations() {
        Permanent possum = addCreatureReady(player1, new RamblingPossum());
        Permanent firstSaddler = addCreatureReady(player1, new RamblingPossum());
        Permanent secondSaddler = addCreatureReady(player1, new RamblingPossum());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstSaddler.getId());
        harness.passBothPriorities();
        saddle(possum);

        assertThat(firstSaddler.isTapped()).isTrue();
        assertThat(secondSaddler.isTapped()).isTrue();
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstSaddler.getId(), secondSaddler.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(secondSaddler.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(possum, firstSaddler)
                .doesNotContain(secondSaddler);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(secondSaddler.getCard().getId()));
        assertThat(gqs.getEffectivePower(gd, possum)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, possum)).isEqualTo(5);
    }

    @Test
    @DisplayName("A creature that saddled the Possum remains eligible after an opponent gains control of it")
    void mayReturnSaddlerNowControlledByOpponent() {
        Permanent possum = addCreatureReady(player1, new RamblingPossum());
        Permanent saddler = addCreatureReady(player1, new RamblingPossum());
        saddler.getCard().setOwnerId(player1.getId());

        saddle(possum);
        gd.playerBattlefields.get(player1.getId()).remove(saddler);
        gd.playerBattlefields.get(player2.getId()).add(saddler);
        gd.stolenCreatures.put(saddler.getId(), player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(saddler.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(saddler.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(saddler);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(saddler.getCard().getId()));
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(saddler.getCard().getId()));
        assertThat(gqs.getEffectivePower(gd, possum)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, possum)).isEqualTo(5);
    }
    private void saddle(Permanent possum) {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(possum.isSaddled()).isTrue();
    }
}
