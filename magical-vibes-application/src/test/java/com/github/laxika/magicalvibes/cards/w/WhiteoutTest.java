package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HyalopterousLemure;
import com.github.laxika.magicalvibes.cards.k.KjeldoranSkyknight;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Whiteout.class, HyalopterousLemure.class, KjeldoranSkyknight.class, Plains.class,
        SnowCoveredPlains.class})
class WhiteoutTest extends BaseCardTest {

    private Permanent snowLand(Player controller) {
        return harness.addToBattlefieldAndReturn(controller, new SnowCoveredPlains());
    }

    private void castWhiteout() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Whiteout()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0);
    }

    @Test
    @DisplayName("All creatures lose flying, both players'")
    void allCreaturesLoseFlying() {
        harness.addToBattlefield(player1, new KjeldoranSkyknight());
        harness.addToBattlefield(player2, new KjeldoranSkyknight());

        castWhiteout();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Kjeldoran Skyknight"), Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Kjeldoran Skyknight"), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying returns at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new KjeldoranSkyknight());

        castWhiteout();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Kjeldoran Skyknight"), Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Kjeldoran Skyknight"), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A later flying grant works after Whiteout resolves")
    void laterFlyingGrantWorks() {
        Permanent lemure = addCreatureReady(player1, new HyalopterousLemure());

        castWhiteout();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lemure, Keyword.FLYING)).isTrue();
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
        harness.assertInGraveyard(player1, "Snow-Covered Plains");
    }

    @Test
    @DisplayName("Graveyard ability returns only the activated Whiteout")
    void graveyardAbilityReturnsOnlyActivatedCard() {
        Permanent snow = snowLand(player1);
        Whiteout activated = new Whiteout();
        Whiteout other = new Whiteout();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(activated, other));

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(activated, other,
                snow.getOriginalCard());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(activated);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other,
                snow.getOriginalCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(snow);
    }

    @Test
    @DisplayName("An opponent's snow land cannot pay Whiteout's graveyard ability")
    void opponentSnowLandCannotPayGraveyardAbility() {
        Permanent opponentSnow = snowLand(player2);
        Whiteout whiteout = new Whiteout();
        harness.setGraveyard(player1, List.of(whiteout));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentSnow);
        harness.assertInGraveyard(player1, "Whiteout");
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
