package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BrambleguardCaptain;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.s.SeasonedWarrenguard;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarvestriteHost.class, BrambleguardCaptain.class, SeasonedWarrenguard.class, Conspiracy.class})
class HarvestriteHostTest extends BaseCardTest {

    @Test
    void ownEntryBoostsTargetWithoutDrawing() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrambleguardCaptain());
        BrambleguardCaptain topCard = new BrambleguardCaptain();
        harness.setLibrary(player1, List.of(topCard));

        castHost();
        resolveTrigger(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCard);
    }

    @Test
    void secondRabbitEntryDrawsCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrambleguardCaptain());
        BrambleguardCaptain drawnCard = new BrambleguardCaptain();
        harness.setLibrary(player1, List.of(drawnCard));

        castHost();
        resolveTrigger(target);
        castRabbit();
        resolveTrigger(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void fizzledTriggerDoesNotCountTowardSecondResolution() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new BrambleguardCaptain());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new BrambleguardCaptain());
        BrambleguardCaptain drawnCard = new BrambleguardCaptain();
        harness.setLibrary(player1, List.of(drawnCard));

        castHost();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstTarget.getId());
        gd.playerBattlefields.get(player1.getId()).remove(firstTarget);
        harness.passBothPriorities();

        castRabbit();
        resolveTrigger(secondTarget);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        castRabbit();
        resolveTrigger(secondTarget);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void thirdResolutionBoostsWithoutDrawingAgain() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrambleguardCaptain());
        BrambleguardCaptain drawnCard = new BrambleguardCaptain();
        BrambleguardCaptain remainingCard = new BrambleguardCaptain();
        harness.setLibrary(player1, List.of(drawnCard, remainingCard));

        castHost();
        resolveTrigger(target);
        castRabbit();
        resolveTrigger(target);
        castRabbit();
        resolveTrigger(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
    }

    @Test
    void nonRabbitEntryDoesNotTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrambleguardCaptain());
        castHost();
        resolveTrigger(target);

        harness.castFromHand(player1, new BrambleguardCaptain(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
    }

    @Test
    void canTargetItselfOnEntry() {
        castHost();
        Permanent host = gd.playerBattlefields.get(player1.getId()).getFirst();

        resolveTrigger(host);

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(3);
    }

    @Test
    void opponentsRabbitDoesNotTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrambleguardCaptain());
        castHost();
        resolveTrigger(target);
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new SeasonedWarrenguard(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
    }

    @Test
    void secondResolutionDrawsEvenIfHostHasLeftBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrambleguardCaptain());
        BrambleguardCaptain drawnCard = new BrambleguardCaptain();
        harness.setLibrary(player1, List.of(drawnCard));
        castHost();
        resolveTrigger(target);
        Permanent host = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof HarvestriteHost)
                .findFirst().orElseThrow();

        castRabbit();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(host);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @CardUsed({Conspiracy.class})
    void ownEntryStillTriggersWhenItIsNotARabbit() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrambleguardCaptain());

        castHost();
        resolveTrigger(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castHost() {
        harness.castFromHand(player1, new HarvestriteHost(), "{2}{W}");
        harness.passBothPriorities();
    }

    private void castRabbit() {
        harness.castFromHand(player1, new SeasonedWarrenguard(), "{W}");
        harness.passBothPriorities();
    }

    private void resolveTrigger(Permanent target) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
