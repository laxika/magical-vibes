package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.c.CliffrunnerBehemoth;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GluttonousSlime.class, CliffrunnerBehemoth.class})
class GluttonousSlimeTest extends BaseCardTest {

    private void castSlime() {
        harness.castFromHand(player1, new GluttonousSlime(), "{2}{G}");
    }

    private Permanent slime() {
        return findPermanent(player1, "Gluttonous Slime");
    }

    @Test
    @DisplayName("Devouring a creature enters with a +1/+1 counter")
    void devourAddsCounter() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CliffrunnerBehemoth());

        castSlime();
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));

        assertThat(slime().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        // Fodder was sacrificed to devour.
        harness.assertNotOnBattlefield(player1, "Cliffrunner Behemoth");
    }

    @Test
    @DisplayName("Devouring nothing enters with no counters")
    void devourNoneNoCounters() {
        harness.addToBattlefield(player1, new CliffrunnerBehemoth());

        castSlime();
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(slime().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        // Choosing to devour nothing keeps the other creature.
        harness.assertOnBattlefield(player1, "Cliffrunner Behemoth");
    }

    @Test
    @DisplayName("With no other creatures, the Slime enters with no counters and no prompt")
    void noOtherCreaturesNoPrompt() {
        castSlime();
        harness.passBothPriorities(); // resolve creature spell (no devour prompt: no other creatures)

        assertThat(slime().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Devour sacrifices any number of controlled creatures and leaves unchosen creatures")
    void devourMultipleCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CliffrunnerBehemoth());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CliffrunnerBehemoth());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new CliffrunnerBehemoth());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new CliffrunnerBehemoth());

        castSlime();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                (PendingInteraction.MultiPermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId(), unchosen.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(slime().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(unchosen)
                .doesNotContain(first, second);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponent);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first.getCard(), second.getCard());
    }

    @Test
    @DisplayName("Opponent creatures alone do not allow devour")
    void opponentCreaturesDoNotPromptDevour() {
        harness.addToBattlefield(player2, new CliffrunnerBehemoth());

        castSlime();
        harness.passBothPriorities();

        assertThat(slime().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Cliffrunner Behemoth");
    }

    @Test
    @DisplayName("Flash allows casting during an opponent's combat and devouring on resolution")
    void flashDuringOpponentsCombat() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CliffrunnerBehemoth());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        castSlime();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        harness.assertNotOnBattlefield(player1, "Gluttonous Slime");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));

        assertThat(slime().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Cliffrunner Behemoth");
    }
}
