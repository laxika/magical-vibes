package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WakingTheTrolls.class, Forest.class})
class WakingTheTrollsTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I destroys a target land")
    void chapterIDestroysTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        addSaga(player1, 0);

        triggerChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Chapter II returns a target land from any graveyard under your control")
    void chapterIIReturnsLandFromAnyGraveyard() {
        Forest target = new Forest();
        harness.setGraveyard(player2, List.of(target));
        addSaga(player1, 1);

        triggerChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(permanent ->
                permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Chapter III creates one Troll for each land deficit")
    void chapterIIICreatesTokensForLandDifference() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        addSaga(player1, 2);

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        List<Permanent> trolls = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Troll Warrior"))
                .toList();
        assertThat(trolls).hasSize(2);
        assertThat(trolls).allSatisfy(troll -> {
            assertThat(troll.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(troll.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(troll.getCard().getSubtypes()).containsExactly(CardSubtype.TROLL, CardSubtype.WARRIOR);
            assertThat(troll.getCard().getKeywords()).contains(Keyword.TRAMPLE);
            assertThat(troll.getEffectivePower()).isEqualTo(4);
            assertThat(troll.getEffectiveToughness()).isEqualTo(4);
        });
    }

    @Test
    @DisplayName("Chapter III creates no tokens when the target is not behind in lands")
    void chapterIIICreatesNoTokensWithoutLandDeficit() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        addSaga(player1, 2);

        triggerChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private Permanent addSaga(com.github.laxika.magicalvibes.model.Player player, int loreCounters) {
        Permanent saga = new Permanent(new WakingTheTrolls());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        gd.playerBattlefields.get(player.getId()).add(saga);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
