package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonGirlAndDevilDinosaur.class, GrizzlyBears.class, Ornithopter.class})
class MoonGirlAndDevilDinosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when an artifact you control enters, only once each turn")
    void artifactEntryDrawsOnlyOnceEachTurn() {
        Permanent source = addCreatureReady(player1, new MoonGirlAndDevilDinosaur());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Ornithopter()));

        castArtifactFromHand();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);

        harness.setHand(player1, List.of(new Ornithopter()));
        castArtifactFromHand();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Drawing the second card sets base power and toughness to 6/6 and grants trample")
    void secondDrawBoostsSourceUntilEndOfTurn() {
        Permanent source = addCreatureReady(player1, new MoonGirlAndDevilDinosaur());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, source, Keyword.TRAMPLE)).isFalse();

        draw(player1);
        resolveTopOfStack();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, source, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, source, Keyword.TRAMPLE)).isFalse();
    }

    private void castArtifactFromHand() {
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
