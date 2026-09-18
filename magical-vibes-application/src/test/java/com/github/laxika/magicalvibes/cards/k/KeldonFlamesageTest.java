package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeldonFlamesage.class, Divination.class, GrizzlyBears.class, Shock.class})
class KeldonFlamesageTest extends BaseCardTest {

    @Test
    void looksAtTopCardsEqualToPowerAndOnlyOffersCheapInstantOrSorcery() {
        Card tooExpensive = new Divination();
        Card nonSpell = new GrizzlyBears();
        Card notLookedAt = new Shock();
        addCreatureReady(player1, new KeldonFlamesage());
        harness.setLibrary(player1, List.of(tooExpensive, nonSpell, notLookedAt));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(tooExpensive, nonSpell, notLookedAt);
    }

    @Test
    void powerSetsHowManyCardsAreLookedAtAndTheFreeCastLimit() {
        Card nonSpell = new GrizzlyBears();
        Card cheapInstant = new Shock();
        Card eligibleSorcery = new Divination();
        Card notLookedAt = new GrizzlyBears();
        Permanent flamesage = addCreatureReady(player1, new KeldonFlamesage());
        flamesage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(nonSpell, cheapInstant, eligibleSorcery, notLookedAt));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(cheapInstant, eligibleSorcery);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(notLookedAt);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(eligibleSorcery);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(eligibleSorcery);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.SORCERY_SPELL
                && entry.getCard() == eligibleSorcery);
    }

    @Test
    void decliningTheFreeCastLeavesTheChosenCardInExile() {
        Card eligibleInstant = new Shock();
        Card rest = new GrizzlyBears();
        addCreatureReady(player1, new KeldonFlamesage());
        harness.setLibrary(player1, List.of(eligibleInstant, rest));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(eligibleInstant);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(rest);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == eligibleInstant);
    }
}
