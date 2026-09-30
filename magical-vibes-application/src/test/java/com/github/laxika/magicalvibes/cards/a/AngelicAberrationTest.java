package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelicAberration.class, GoblinPiker.class, GrizzlyBears.class, Ornithopter.class})
class AngelicAberrationTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices selected creatures with base power or toughness 1 or less")
    void sacrificesSmallCreaturesAndCreatesMatchingAngels() {
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());
        ornithopter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent goblinPiker = addCreatureReady(player1, new GoblinPiker());
        goblinPiker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent grizzlyBears = addCreatureReady(player1, new GrizzlyBears());
        grizzlyBears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.setHand(player1, List.of(new AngelicAberration()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.SacrificeAnyNumberAndRecordCount.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(ornithopter.getId(), goblinPiker.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(ornithopter.getId(), goblinPiker.getId()));

        assertThat(findPermanents(player1, "Eldrazi Angel")).hasSize(2).allSatisfy(angel -> {
            assertThat(angel.getCard().getSubtypes()).containsExactlyInAnyOrder(
                    CardSubtype.ELDRAZI,
                    CardSubtype.ANGEL);
            assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
            assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();
        });
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(grizzlyBears);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Ornithopter", "Goblin Piker");
    }
}
