package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RobeOfMirrors;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OathOfEorl.class, GrizzlyBears.class, YouthfulKnight.class, RobeOfMirrors.class})
class OathOfEorlTest extends BaseCardTest {

    @Test
    void chapterICreatesTwoHumanSoldiers() {
        addSagaWithLore(0);

        triggerNextChapter();
        harness.passBothPriorities();

        List<Permanent> tokens = tokenPermanents(CardSubtype.SOLDIER);
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        });
    }

    @Test
    void chapterIICreatesTwoHastyTramplingHumanKnights() {
        addSagaWithLore(1);

        triggerNextChapter();
        harness.passBothPriorities();

        List<Permanent> tokens = tokenPermanents(CardSubtype.KNIGHT);
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.KNIGHT);
            assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
            assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        });
    }

    @Test
    void chapterIIIPutsAnIndestructibleCounterOnAHumanAndMakesControllerMonarch() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addSagaWithLore(2);

        triggerNextChapter();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).contains(human.getId());
        assertThat(targetChoice.validPermanentIds()).doesNotContain(nonHuman.getId());

        harness.handlePermanentChosen(player1, human.getId());
        harness.passBothPriorities();

        assertThat(human.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof OathOfEorl);
    }

    @Test
    void castingSagaTriggersChapterIAndNextMainPhaseTriggersChapterII() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new OathOfEorl(), "{3}{R}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(tokenPermanents(CardSubtype.SOLDIER)).hasSize(2).allSatisfy(token ->
                assertThat(token.getCard().getColors()).containsExactly(CardColor.WHITE));
        assertThat(tokenPermanents(CardSubtype.KNIGHT)).isEmpty();
        Permanent saga = findPermanent(player1, "Oath of Eorl");
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);

        triggerNextChapter();
        harness.passBothPriorities();

        assertThat(tokenPermanents(CardSubtype.KNIGHT)).hasSize(2).allSatisfy(token ->
                assertThat(token.getCard().getColors()).containsExactly(CardColor.RED));
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Oath of Eorl");
    }

    @Test
    void chapterIIIWithNoHumansStillMakesControllerMonarch() {
        addSagaWithLore(2);

        triggerNextChapter();
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        harness.assertNotOnBattlefield(player1, "Oath of Eorl");
        harness.assertInGraveyard(player1, "Oath of Eorl");
    }

    @Test
    void chapterIIICanChooseZeroTargetsEvenWhenAHumanIsAvailable() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        addSagaWithLore(2);
        gd.monarchPlayerId = player2.getId();

        triggerNextChapter();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(human.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        harness.assertInGraveyard(player1, "Oath of Eorl");
    }

    @Test
    void chapterIIICanTargetAnOpponentsHumanButMakesItsOwnControllerMonarch() {
        Permanent human = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        addSagaWithLore(2);
        gd.monarchPlayerId = player2.getId();

        triggerNextChapter();
        harness.handlePermanentChosen(player1, human.getId());
        harness.passBothPriorities();

        assertThat(human.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void chapterIIIDoesNotOfferAHumanWithShroudAsATarget() {
        Permanent protectedHuman = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent robe = harness.addToBattlefieldAndReturn(player1, new RobeOfMirrors());
        robe.setAttachedTo(protectedHuman.getId());
        Permanent legalHuman = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        addSagaWithLore(2);
        assertThat(gqs.hasKeyword(gd, protectedHuman, Keyword.SHROUD)).isTrue();

        triggerNextChapter();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).contains(legalHuman.getId());
        assertThat(targetChoice.validPermanentIds()).doesNotContain(protectedHuman.getId());
        harness.handlePermanentChosen(player1, legalHuman.getId());
        harness.passBothPriorities();

        assertThat(protectedHuman.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(legalHuman.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void chapterIIIDoesNotMakeControllerMonarchWhenItsOnlyTargetGainsShroud() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        addSagaWithLore(2);
        gd.monarchPlayerId = player2.getId();

        triggerNextChapter();
        harness.handlePermanentChosen(player1, human.getId());
        Permanent robe = harness.addToBattlefieldAndReturn(player1, new RobeOfMirrors());
        robe.setAttachedTo(human.getId());
        assertThat(gqs.hasKeyword(gd, human, Keyword.SHROUD)).isTrue();
        harness.passBothPriorities();

        assertThat(human.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
        harness.assertInGraveyard(player1, "Oath of Eorl");
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new OathOfEorl());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
    }

    private List<Permanent> tokenPermanents(CardSubtype subtype) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(subtype))
                .toList();
    }
}
