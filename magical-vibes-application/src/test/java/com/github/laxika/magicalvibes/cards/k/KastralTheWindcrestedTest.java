package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KastralTheWindcrested.class, SuntailHawk.class})
class KastralTheWindcrestedTest extends BaseCardTest {

    private static final String REANIMATE =
            "Put a Bird creature card onto the battlefield with a finality counter on it";
    private static final String COUNTER = "Put a +1/+1 counter on each Bird you control";
    private static final String DRAW = "Draw a card";

    @Test
    void putsBirdFromHandOntoBattlefieldWithFinalityCounter() {
        Card bird = new SuntailHawk();
        harness.setHand(player1, List.of(bird));
        addAttackingKastral();

        resolveKastralTrigger();
        harness.handleListChoice(player1, REANIMATE);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(bird.getId()));

        Permanent entered = findPermanentByCardId(bird.getId());
        assertThat(entered.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bird);
    }

    @Test
    void putsBirdFromGraveyardOntoBattlefieldWithFinalityCounter() {
        Card bird = new SuntailHawk();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(bird));
        addAttackingKastral();

        resolveKastralTrigger();
        harness.handleListChoice(player1, REANIMATE);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(bird.getId()));

        Permanent entered = findPermanentByCardId(bird.getId());
        assertThat(entered.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bird);
    }

    @Test
    void mayDeclinePuttingBirdOntoBattlefield() {
        Card bird = new SuntailHawk();
        harness.setHand(player1, List.of(bird));
        addAttackingKastral();

        resolveKastralTrigger();
        harness.handleListChoice(player1, REANIMATE);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bird);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(bird.getId()));
    }

    @Test
    void putsCountersOnEachBirdYouControl() {
        Permanent kastral = addAttackingKastral();
        Permanent hawk = addAttacking(new SuntailHawk());

        resolveKastralTrigger();
        harness.handleListChoice(player1, COUNTER);
        harness.passBothPriorities();

        assertThat(kastral.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hawk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void drawsACard() {
        Card draw = new SuntailHawk();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(draw));
        addAttackingKastral();

        resolveKastralTrigger();
        harness.handleListChoice(player1, DRAW);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    private Permanent addAttackingKastral() {
        return addAttacking(new KastralTheWindcrested());
    }

    private Permanent addAttacking(Card card) {
        Permanent permanent = addCreatureReady(player1, card);
        permanent.setAttacking(true);
        return permanent;
    }

    private void resolveKastralTrigger() {
        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    private Permanent findPermanentByCardId(UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }
}
