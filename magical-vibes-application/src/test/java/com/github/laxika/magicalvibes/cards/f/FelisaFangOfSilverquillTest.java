package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FelisaFangOfSilverquill.class, GrizzlyBears.class, WrathOfGod.class})
class FelisaFangOfSilverquillTest extends BaseCardTest {

    @Test
    @DisplayName("Mentor targets an attacking creature with lesser power")
    void mentorTargetsAttackingCreatureWithLesserPower() {
        addCreatureReady(player1, new FelisaFangOfSilverquill());
        Permanent attackingCreature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(attackingCreature.getId());

        harness.handlePermanentChosen(player1, attackingCreature.getId());
        resolveAllTriggers();

        assertThat(attackingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A nontoken creature with counters dying creates that many tapped Inklings")
    void deathCreatesTappedInklingsForEachCounter() {
        harness.addToBattlefield(player1, new FelisaFangOfSilverquill());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        dyingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        dyingCreature.setCounterCount(CounterType.CHARGE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> inklings = findPermanents(player1, "Inkling");
        assertThat(inklings).hasSize(3);
        assertThat(inklings).allSatisfy(inkling -> {
            assertThat(inkling.isTapped()).isTrue();
            assertThat(inkling.getCard().getPower()).isEqualTo(2);
            assertThat(inkling.getCard().getToughness()).isEqualTo(1);
            assertThat(inkling.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
            assertThat(inkling.getCard().getSubtypes()).contains(CardSubtype.INKLING);
            assertThat(inkling.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(inkling.getCard().hasType(CardType.CREATURE)).isTrue();
        });
    }

    @Test
    @DisplayName("A nontoken creature without counters creates no Inklings")
    void deathWithoutCountersCreatesNoInklings() {
        harness.addToBattlefield(player1, new FelisaFangOfSilverquill());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Inkling")).isEmpty();
    }
}
