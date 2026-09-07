package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheGitrogRavenousRide.class, GrizzlyBears.class, Forest.class})
class TheGitrogRavenousRideTest extends BaseCardTest {

    @Test
    @DisplayName("sacrifices a saddler, draws its power, and puts that many lands from hand tapped")
    void sacrificesSaddlerDrawsAndPutsLands() {
        Permanent gitrog = addCreatureReady(player1, new TheGitrogRavenousRide());
        Permanent saddler = addCreatureReady(player1, new GrizzlyBears());
        Forest forestOne = new Forest();
        Forest forestTwo = new Forest();
        harness.setHand(player1, List.of(forestOne, forestTwo));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.creaturesThatSaddledPermanentThisTurn.get(gitrog.getId()))
                .containsExactly(saddler.getId());

        declareAttackers(player1, List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, saddler.getId());
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(forestOne.getId(), forestTwo.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saddler);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Forest")))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2)
                .allMatch(card -> card.getName().equals("Grizzly Bears"));
    }
}
