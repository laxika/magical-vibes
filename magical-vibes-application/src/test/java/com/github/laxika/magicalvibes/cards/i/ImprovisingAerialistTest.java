package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImprovisingAerialist.class, Forest.class, StormCrow.class, GrizzlyBears.class})
class ImprovisingAerialistTest extends BaseCardTest {

    @Test
    void tappedSurvivorPerpetuallyGivesFlyingToItselfAndTopNonFlyingCreature() {
        Permanent aerialist = harness.addToBattlefieldAndReturn(player1, new ImprovisingAerialist());
        Card nonCreature = new Forest();
        Card flyingCreature = new StormCrow();
        Card nonFlyingCreature = new GrizzlyBears();
        aerialist.tap();
        harness.setLibrary(player1, List.of(nonCreature, flyingCreature, nonFlyingCreature));

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(gqs.hasKeyword(gd, aerialist, Keyword.FLYING)).isTrue();
        assertThat(library).hasSize(3);
        assertThat(library.get(0)).isSameAs(nonCreature);
        assertThat(library.get(1)).isSameAs(flyingCreature);
        assertThat(library.get(2).getId()).isEqualTo(nonFlyingCreature.getId());
        assertThat(library.get(2).hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(nonFlyingCreature.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void stillGivesItselfFlyingWhenLibraryHasNoMatchingCreature() {
        Permanent aerialist = harness.addToBattlefieldAndReturn(player1, new ImprovisingAerialist());
        Card nonCreature = new Forest();
        Card flyingCreature = new StormCrow();
        aerialist.tap();
        harness.setLibrary(player1, List.of(nonCreature, flyingCreature));

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(gqs.hasKeyword(gd, aerialist, Keyword.FLYING)).isTrue();
        assertThat(library).containsExactly(nonCreature, flyingCreature);
    }

    @Test
    void untappedSurvivorDoesNotTrigger() {
        harness.addToBattlefieldAndReturn(player1, new ImprovisingAerialist());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
