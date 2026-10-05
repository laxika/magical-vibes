package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.ErdwalIlluminator;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnowledgeSeeker.class, GrizzlyBears.class, Shock.class, ErdwalIlluminator.class})
class KnowledgeSeekerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when its controller draws their second card each turn")
    void triggersOnControllerSecondCardDraw() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new KnowledgeSeeker());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        drawAndResolveTrigger(player1);
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        drawAndResolveTrigger(player1);
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        drawAndResolveTrigger(player1);
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates a Clue when it dies")
    void createsClueWhenItDies() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new KnowledgeSeeker());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, seeker.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Knowledge Seeker");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void opponentsSecondDrawDoesNotTrigger() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new KnowledgeSeeker());
        harness.setLibrary(player2, List.of(new KnowledgeSeeker(), new KnowledgeSeeker()));

        drawAndResolveTrigger(player2);
        drawAndResolveTrigger(player2);

        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void secondDrawOnOpponentsTurnTriggersEvenWhenFirstDrawPrecededEntry() {
        harness.forceActivePlayer(player2);
        harness.setLibrary(player1, List.of(new KnowledgeSeeker(), new KnowledgeSeeker()));
        drawAndResolveTrigger(player1);
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new KnowledgeSeeker());

        drawAndResolveTrigger(player1);

        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void deathCreatesClueWithoutInvestigating() {
        harness.addToBattlefield(player1, new ErdwalIlluminator());
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new KnowledgeSeeker());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, seeker.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Knowledge Seeker");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void enteringAfterSecondDrawDoesNotTriggerOnThirdDraw() {
        harness.setLibrary(player1, List.of(new KnowledgeSeeker(), new KnowledgeSeeker(), new KnowledgeSeeker()));
        drawAndResolveTrigger(player1);
        drawAndResolveTrigger(player1);
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new KnowledgeSeeker());

        drawAndResolveTrigger(player1);

        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void createdClueCanBeSacrificedForTwoManaToDraw() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new KnowledgeSeeker());
        harness.setLibrary(player1, List.of(new KnowledgeSeeker()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, seeker.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Clue");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLibraries.get(player1.getId())).isEmpty();
    }

    private void drawAndResolveTrigger(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        resolveAllTriggers();
    }
}
