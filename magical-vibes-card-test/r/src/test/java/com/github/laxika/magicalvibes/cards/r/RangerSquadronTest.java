package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RangerSquadron.class)
class RangerSquadronTest extends BaseCardTest {

    @Test
    void laterAttacksByTheOriginalAndConjuredSquadronDoNotConjureAgain() {
        Permanent original = addCreatureReady(player1, new RangerSquadron());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        gd.playerHands.get(player1.getId()).remove(duplicate);
        Permanent conjured = harness.enterBattlefieldAndReturn(player1, duplicate);
        harness.performUntapStep(player1);
        conjured.setSummoningSick(false);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gqs.hasKeyword(gd, original, Keyword.DOUBLE_TEAM)).isFalse();
        assertThat(gqs.hasKeyword(gd, conjured, Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    void eachAttackingSquadronConjuresItsOwnDuplicate() {
        Permanent first = addCreatureReady(player1, new RangerSquadron());
        Permanent second = addCreatureReady(player1, new RangerSquadron());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2)
                .allSatisfy(card -> {
                    assertThat(card).isInstanceOf(RangerSquadron.class);
                    assertThat(card.getKeywords()).doesNotContain(Keyword.DOUBLE_TEAM);
                    assertThat(card.getId()).isNotEqualTo(first.getCard().getId())
                            .isNotEqualTo(second.getCard().getId());
                });
        assertThat(gd.playerHands.get(player1.getId()).get(0).getId())
                .isNotEqualTo(gd.playerHands.get(player1.getId()).get(1).getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_TEAM)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_TEAM)).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void doubleTeamConjuresADuplicateAndRemovesDoubleTeamFromBothCards() {
        Permanent squadron = addCreatureReady(player1, new RangerSquadron());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, squadron, Keyword.DOUBLE_TEAM)).isFalse();
        Card copy = gd.playerHands.get(player1.getId()).stream()
                .filter(RangerSquadron.class::isInstance)
                .findFirst()
                .orElseThrow();
        assertThat(copy.getKeywords()).doesNotContain(Keyword.DOUBLE_TEAM);
    }
}
