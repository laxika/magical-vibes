package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeddlingYouths.class, GrizzlyBears.class})
class MeddlingYouthsTest extends BaseCardTest {

    @Test
    void investigatesWhenThreeCreaturesAttack() {
        addCreatureReady(player1, new MeddlingYouths());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2, 3));
        harness.passBothPriorities();

        List<Permanent> clues = findPermanents(player1, "Clue");
        assertThat(clues).hasSize(1);
        assertThat(clues.getFirst().getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(clues.getFirst().getCard().getSubtypes()).contains(CardSubtype.CLUE);
    }

    @Test
    void doesNotInvestigateWhenFewerThanThreeCreaturesAttack() {
        addCreatureReady(player1, new MeddlingYouths());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @CardUsed({MeddlingYouths.class})
    void eachCopyInvestigatesOnlyOnceWhenFourCreaturesAttack() {
        for (int i = 0; i < 4; i++) {
            addCreatureReady(player1, new MeddlingYouths());
        }

        declareAttackers(List.of(0, 1, 2, 3));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Clue")).isEqualTo(4);
        assertThat(countPermanents(player2, "Clue")).isZero();
    }

    @Test
    @CardUsed({MeddlingYouths.class})
    void opposingAttackDoesNotTriggerDefendersYouths() {
        addCreatureReady(player1, new MeddlingYouths());
        for (int i = 0; i < 3; i++) {
            addCreatureReady(player2, new MeddlingYouths());
        }

        declareAttackers(player2, List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Clue")).isZero();
        assertThat(countPermanents(player2, "Clue")).isEqualTo(3);
    }

    @Test
    void investigatesEvenWhenAttackersLeaveBeforeResolution() {
        addCreatureReady(player1, new MeddlingYouths());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2, 3));
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard() instanceof GrizzlyBears);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Clue")).isEqualTo(1);
    }
}
