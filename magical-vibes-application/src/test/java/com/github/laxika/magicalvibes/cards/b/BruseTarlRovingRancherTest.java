package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TimeWarp;
import com.github.laxika.magicalvibes.cards.y.YokedOx;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BruseTarlRovingRancher.class, GrizzlyBears.class, LlanowarElves.class,
        Plains.class, TimeWarp.class, YokedOx.class})
class BruseTarlRovingRancherTest extends BaseCardTest {

    @Test
    @DisplayName("Oxen you control have double strike")
    void oxenHaveDoubleStrike() {
        addCreatureReady(player1, new BruseTarlRovingRancher());
        Permanent ox = addCreatureReady(player1, new YokedOx());
        Permanent nonOx = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ox, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonOx, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Entering with a land creates a 2/2 white Ox token")
    void landEntryCreatesOxToken() {
        harness.setHand(player1, List.of(new BruseTarlRovingRancher()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent oxToken = findPermanent(player1, "Ox");
        assertThat(oxToken).isNotNull();
        assertThat(oxToken.getCard().getPower()).isEqualTo(2);
        assertThat(oxToken.getCard().getToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, oxToken, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getName)
                .containsExactly("Plains");
    }

    @Test
    @DisplayName("Entering with a nonland grants cast permission until the end of the next turn")
    void nonlandEntryGrantsNextTurnCastPermission() {
        harness.setHand(player1, List.of(new BruseTarlRovingRancher()));
        LlanowarElves topCard = new LlanowarElves();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).containsKey(topCard.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
    }

    @Test
    @DisplayName("Attacking repeats the top-card branch")
    void attackingGrantsCastPermission() {
        Permanent bruse = addCreatureReady(player1, new BruseTarlRovingRancher());
        LlanowarElves topCard = new LlanowarElves();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(battlefieldIndex(bruse)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    void opposingOxDoesNotGainDoubleStrike() {
        addCreatureReady(player1, new BruseTarlRovingRancher());
        Permanent ox = addCreatureReady(player2, new YokedOx());

        assertThat(gqs.hasKeyword(gd, ox, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void oxLosesDoubleStrikeWhenBruseLeaves() {
        Permanent bruse = addCreatureReady(player1, new BruseTarlRovingRancher());
        Permanent ox = addCreatureReady(player1, new YokedOx());
        assertThat(gqs.hasKeyword(gd, ox, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(bruse);

        assertThat(gqs.hasKeyword(gd, ox, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void emptyLibraryCreatesNoTokenOrPermission() {
        harness.setLibrary(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new BruseTarlRovingRancher());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Ox")).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    void attackingWithLandCreatesOxAndDoesNotPermitPlayingLand() {
        Permanent bruse = addCreatureReady(player1, new BruseTarlRovingRancher());
        Plains land = new Plains();
        harness.setLibrary(player1, List.of(land));

        declareAttackers(List.of(battlefieldIndex(bruse)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Ox")).isEqualTo(1);
        Permanent ox = findPermanent(player1, "Ox");
        assertThat(ox.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, ox, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land.getId());
    }

    @Test
    void nonlandCanBeCastWithItsNormalManaCostAfterBruseLeaves() {
        LlanowarElves topCard = new LlanowarElves();
        harness.setLibrary(player1, List.of(topCard));
        Permanent bruse = harness.enterBattlefieldAndReturn(player1, new BruseTarlRovingRancher());
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(bruse);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromExile(player1, topCard.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Llanowar Elves")).isNotNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Ox")).isZero();
    }

    @Test
    void permissionLastsThroughNextOwnTurnAndThenExpires() {
        LlanowarElves topCard = new LlanowarElves();
        harness.setLibrary(player1, List.of(topCard, new Plains(), new Plains(), new Plains()));
        harness.setLibrary(player2, List.of(new Plains(), new Plains(), new Plains()));
        harness.enterBattlefieldAndReturn(player1, new BruseTarlRovingRancher());
        resolveAllTriggers();

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.forceStep(TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    void permissionExpiresAtEndOfNextOwnTurnEvenWhenItIsAnExtraTurn() {
        LlanowarElves topCard = new LlanowarElves();
        harness.setLibrary(player1, List.of(topCard, new Plains(), new Plains(), new Plains()));
        harness.setLibrary(player2, List.of(new Plains(), new Plains()));
        harness.enterBattlefieldAndReturn(player1, new BruseTarlRovingRancher());
        resolveAllTriggers();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TimeWarp()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    void permissionDoesNotWaiveManaCost() {
        LlanowarElves topCard = new LlanowarElves();
        harness.setLibrary(player1, List.of(topCard));
        harness.enterBattlefieldAndReturn(player1, new BruseTarlRovingRancher());
        resolveAllTriggers();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(countPermanents(player1, "Llanowar Elves")).isZero();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
