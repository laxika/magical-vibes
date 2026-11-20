package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MordorOnTheMarch.class, GrizzlyBears.class, LlanowarElves.class, Forest.class})
class MordorOnTheMarchTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the creature card from your graveyard, copies it with haste and exiles the copy at the next end step")
    void createsHastyCopyAndExilesItAtTheNextEndStep() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MordorOnTheMarch()));
        addMana();

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Grizzly Bears");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(als.canAttack(gd, token, gd.findControllerOf(token))).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    @Test
    @DisplayName("The creature card is chosen as the spell resolves, not targeted, and only creature cards are offered")
    void exilesACreatureCardChosenOnResolution() {
        Card land = new Forest();
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(land, bears, elves));
        harness.setHand(player1, List.of(new MordorOnTheMarch()));
        addMana();

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleGraveyardCardChosen(player1, 2);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(elves).doesNotContain(bears, land);
        assertThat(findPermanent(player1, "Llanowar Elves").getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("With no creature card in your graveyard the spell does nothing")
    void noCreatureCardMeansNoCopy() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setHand(player1, List.of(new MordorOnTheMarch()));
        addMana();

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
    }

    @Test
    @DisplayName("Storm copies the spell for each spell cast before it, each copy exiling and copying a creature card")
    void stormCopiesTheSpellForEachSpellCastBeforeItThisTurn() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new MordorOnTheMarch()));
        addMana();
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());

        harness.castSorcery(player1, 0);
        resolveAllTriggers();
        while (gd.interaction.isAwaitingInput()) {
            harness.handleGraveyardCardChosen(player1, 0);
            resolveAllTriggers();
        }

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(3);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
