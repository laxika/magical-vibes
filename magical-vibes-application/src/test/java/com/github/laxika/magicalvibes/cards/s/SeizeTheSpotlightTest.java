package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeizeTheSpotlight.class, GiantSpider.class, GrizzlyBears.class})
class SeizeTheSpotlightTest extends BaseCardTest {

    @Test
    void fortuneDrawsAndCreatesTreasure() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castSpell();

        harness.handleListChoice(player2, ChoiceContext.SeizeTheSpotlightChoice.FORTUNE);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void fameLetsControllerChooseCreatureAndTemporaryControlExpires() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GiantSpider()).tap();
        bears.tap();
        castSpell();

        harness.handleListChoice(player2, ChoiceContext.SeizeTheSpotlightChoice.FAME);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();

        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(bears.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(bears.getId())).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(bears.getId())).isFalse();
    }

    private void castSpell() {
        harness.setHand(player1, List.of(new SeizeTheSpotlight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactlyElementsOf(ChoiceContext.SeizeTheSpotlightChoice.OPTIONS);
    }
}
