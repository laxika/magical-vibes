package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.k.KnightOfTheWhiteOrchid;
import com.github.laxika.magicalvibes.cards.c.CleansingNova;
import com.github.laxika.magicalvibes.cards.s.SpittingImage;
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

@CardUsed({FelisaFangOfSilverquill.class, KnightOfTheWhiteOrchid.class, CleansingNova.class, SpittingImage.class})
class FelisaFangOfSilverquillTest extends BaseCardTest {

    @Test
    @DisplayName("Mentor targets an attacking creature with lesser power")
    void mentorTargetsAttackingCreatureWithLesserPower() {
        addCreatureReady(player1, new FelisaFangOfSilverquill());
        Permanent attackingCreature = addCreatureReady(player1, new KnightOfTheWhiteOrchid());

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
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new KnightOfTheWhiteOrchid());
        dyingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        dyingCreature.setCounterCount(CounterType.CHARGE, 1);

        destroyAllCreatures();

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
        harness.addToBattlefield(player1, new KnightOfTheWhiteOrchid());

        destroyAllCreatures();

        assertThat(findPermanents(player1, "Inkling")).isEmpty();
    }

    @Test
    void mentorRechecksLesserPowerWhenResolving() {
        addCreatureReady(player1, new FelisaFangOfSilverquill());
        Permanent target = addCreatureReady(player1, new KnightOfTheWhiteOrchid());
        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mentorExcludesEqualPowerAndNonattackingCreatures() {
        addCreatureReady(player1, new FelisaFangOfSilverquill());
        Permanent equalPower = addCreatureReady(player1, new KnightOfTheWhiteOrchid());
        equalPower.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent legalTarget = addCreatureReady(player1, new KnightOfTheWhiteOrchid());
        addCreatureReady(player1, new KnightOfTheWhiteOrchid());

        declareAttackers(List.of(0, 1, 2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(legalTarget.getId());
        harness.handlePermanentChosen(player1, legalTarget.getId());
        resolveAllTriggers();
        assertThat(legalTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(equalPower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void felisasOwnDeathCountsAllItsCounters() {
        Permanent felisa = harness.addToBattlefieldAndReturn(player1, new FelisaFangOfSilverquill());
        felisa.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        felisa.setCounterCount(CounterType.CHARGE, 1);
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new KnightOfTheWhiteOrchid());
        ally.setCounterCount(CounterType.CHARGE, 2);

        destroyAllCreatures();

        assertThat(findPermanents(player1, "Inkling")).hasSize(5)
                .allSatisfy(token -> assertThat(token.isTapped()).isTrue());
    }

    @Test
    void opposingCreatureWithCountersCreatesNoInklings() {
        harness.addToBattlefield(player1, new FelisaFangOfSilverquill());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new KnightOfTheWhiteOrchid());
        opponent.setCounterCount(CounterType.CHARGE, 3);

        destroyAllCreatures();

        assertThat(findPermanents(player1, "Inkling")).isEmpty();
        assertThat(findPermanents(player2, "Inkling")).isEmpty();
    }

    @Test
    void tokenCreatureWithCountersCreatesNoInklings() {
        harness.addToBattlefield(player2, new FelisaFangOfSilverquill());
        Permanent original = harness.addToBattlefieldAndReturn(player1, new KnightOfTheWhiteOrchid());
        Permanent token = copyForPlayerTwo(original);
        token.setCounterCount(CounterType.CHARGE, 3);

        destroyAllCreatures();

        assertThat(findPermanents(player2, "Inkling")).isEmpty();
    }

    @Test
    void tokenCopyOfFelisaDoesNotTriggerForItsOwnDeath() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new FelisaFangOfSilverquill());
        Permanent token = copyForPlayerTwo(original);
        token.setCounterCount(CounterType.CHARGE, 3);

        destroyAllCreatures();

        assertThat(findPermanents(player1, "Inkling")).isEmpty();
        assertThat(findPermanents(player2, "Inkling")).isEmpty();
    }

    private Permanent copyForPlayerTwo(Permanent original) {
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new SpittingImage()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player2, 0, original.getId());
        Permanent copy = findPermanent(player2, original.getCard().getName());
        assertThat(copy.getCard().isToken()).isTrue();
        return copy;
    }

    private void destroyAllCreatures() {
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new CleansingNova()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.castAndResolveSorcery(player2, 0, 0);
        resolveAllTriggers();
    }
}
