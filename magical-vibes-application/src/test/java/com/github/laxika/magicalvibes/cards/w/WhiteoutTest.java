package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WhiteoutTest extends BaseCardTest {

    private Permanent snowLand(Player controller) {
        Permanent snowLand = new Permanent(new Plains());
        TestCards.mutableCard(snowLand).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        gd.playerBattlefields.get(controller.getId()).add(snowLand);
        return snowLand;
    }

    private void castWhiteout() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Whiteout()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("All creatures lose flying, both players'")
    void allCreaturesLoseFlying() {
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new SuntailHawk());

        castWhiteout();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Serra Angel"), Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Suntail Hawk"), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying returns at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new SuntailHawk());

        castWhiteout();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Suntail Hawk"), Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Suntail Hawk"), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Sacrificing a snow land returns Whiteout from the graveyard to hand")
    void graveyardAbilityReturnsToHand() {
        Permanent snow = snowLand(player1);
        harness.setGraveyard(player1, List.of(new Whiteout()));

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Whiteout");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(snow);
        harness.assertInGraveyard(player1, "Plains");
    }

    @Test
    @DisplayName("Cannot activate without a snow land")
    void cannotActivateWithoutSnowLand() {
        harness.addToBattlefield(player1, new Plains());
        harness.setGraveyard(player1, List.of(new Whiteout()));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Whiteout");
    }
}
