package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TrainTroops;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImodanesRecruiter.class, TrainTroops.class, GrizzlyBears.class})
class ImodanesRecruiterTest extends BaseCardTest {

    @Test
    void adventureCreatesTwoVigilantKnightsAndExilesCard() {
        ImodanesRecruiter card = new ImodanesRecruiter();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        List<Permanent> knights = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.KNIGHT))
                .toList();
        assertThat(knights).hasSize(2);
        assertThat(knights).allSatisfy(knight -> {
            assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isTrue();
        });
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void enteringBoostsAndGivesHasteToCreaturesYouControlIncludingItself() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castRecruiter();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isTrue();

        Permanent recruiter = findPermanent(player1, "Imodane's Recruiter");
        assertThat(gqs.getEffectivePower(gd, recruiter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recruiter)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, recruiter, Keyword.HASTE)).isTrue();

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    void boostAndHasteExpireAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castRecruiter();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isFalse();
    }

    private void castRecruiter() {
        harness.setHand(player1, List.of(new ImodanesRecruiter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
