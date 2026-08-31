package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.PestProblem;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RatcatcherTrainee.class, PestProblem.class})
class RatcatcherTraineeTest extends BaseCardTest {

    @Test
    void adventureCreatesTwoRatsThatCannotBlockAndExilesTheCard() {
        RatcatcherTrainee card = new RatcatcherTrainee();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        List<Permanent> rats = findPermanents(player1, "Rat").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(rats).hasSize(2);
        assertThat(rats).allMatch(rat -> !bls.canBlock(gd, rat));
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureFaceHasFirstStrikeOnlyDuringItsControllersTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RatcatcherTrainee());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        RatcatcherTrainee card = new RatcatcherTrainee();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
    }
}
