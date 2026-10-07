package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.u.UndercityDireRat;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheRegalia.class, Forest.class, UndercityDireRat.class})
class TheRegaliaTest extends BaseCardTest {

    @Test
    void crewAnimatesTheRegaliaAndTapsTheCrew() {
        Permanent regalia = addRegaliaReady(player1);
        Permanent crew = addCreatureReady(player1, new UndercityDireRat());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, regalia)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void attackingRevealsLandIntoBattlefieldTappedAndRandomizesTheRestToBottom() {
        addRegaliaReady(player1);
        addCreatureReady(player1, new UndercityDireRat());
        Card firstNonland = new TheRegalia();
        Card secondNonland = new UndercityDireRat();
        Card land = new Forest();
        Card topAfterReveal = new Forest();
        harness.setLibrary(player1, List.of(firstNonland, secondNonland, land, topAfterReveal));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == land && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topAfterReveal);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstNonland, secondNonland, topAfterReveal);
    }

    @Test
    void newlyEnteredRegaliaCanAttackWhenCrewedByNewlyEnteredCreature() {
        harness.addToBattlefield(player1, new TheRegalia());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new UndercityDireRat());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == land && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void libraryWithoutLandsReturnsEveryRevealedCardToLibrary() {
        addRegaliaReady(player1);
        addCreatureReady(player1, new UndercityDireRat());
        Card first = new TheRegalia();
        Card second = new UndercityDireRat();
        harness.setLibrary(player1, List.of(first, second));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void attackingWithEmptyLibraryDoesNotPutAnythingOntoBattlefield() {
        addRegaliaReady(player1);
        addCreatureReady(player1, new UndercityDireRat());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    private Permanent addRegaliaReady(Player player) {
        return addCreatureReady(player, new TheRegalia());
    }
}
