package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Anathemancer.class, GhostQuarter.class, Forest.class, Terminate.class})
class AnathemancerTest extends BaseCardTest {

    private void castAnathemancerTargeting(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Anathemancer()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, 0, targetPlayerId);

        resolveAllTriggers();
    }

    @Test
    @DisplayName("ETB deals damage to target player equal to their nonbasic lands; basics don't count")
    void etbDamageEqualsNonbasicLandCount() {
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.addToBattlefield(player2, new Forest()); // basic — ignored
        harness.setLife(player2, 20);

        castAnathemancerTargeting(player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB deals no damage when target player controls no nonbasic lands")
    void etbNoDamageWithOnlyBasics() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player2, 20);

        castAnathemancerTargeting(player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB can target its own controller, counting that player's nonbasic lands")
    void etbCanTargetSelf() {
        harness.addToBattlefield(player1, new GhostQuarter());
        harness.setLife(player1, 20);

        castAnathemancerTargeting(player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    void etbCountsLandsAtResolutionAndIgnoresOtherPlayersAndNonlands() {
        harness.addToBattlefield(player1, new GhostQuarter());
        harness.addToBattlefield(player2, new Anathemancer());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Anathemancer()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.addToBattlefield(player2, new GhostQuarter());
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    void unearthReturnsWithHasteAndTriggersDamage() {
        harness.addToBattlefield(player2, new GhostQuarter());

        unearthTargetingOpponent();

        harness.assertOnBattlefield(player1, "Anathemancer");
        harness.assertNotInGraveyard(player1, "Anathemancer");
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Anathemancer"), Keyword.HASTE)).isTrue();
        harness.assertLife(player2, 19);
    }

    @Test
    void unearthedCreatureIsExiledAtNextEndStep() {
        unearthTargetingOpponent();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Anathemancer");
        harness.assertNotInGraveyard(player1, "Anathemancer");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof Anathemancer);
    }

    @Test
    void destroyingUnearthedCreatureExilesItInsteadOfPuttingItInGraveyard() {
        unearthTargetingOpponent();
        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Anathemancer").getId());

        harness.assertNotOnBattlefield(player1, "Anathemancer");
        harness.assertNotInGraveyard(player1, "Anathemancer");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof Anathemancer);
    }

    @Test
    void unearthCannotBeActivatedOutsideMainPhase() {
        prepareUnearth();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Anathemancer");
        harness.assertNotOnBattlefield(player1, "Anathemancer");
    }

    @Test
    void unearthRequiresFullSevenManaCost() {
        harness.setGraveyard(player1, List.of(new Anathemancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Anathemancer");
        harness.assertNotOnBattlefield(player1, "Anathemancer");
    }

    @Test
    void etbStillDealsDamageAfterSourceIsDestroyed() {
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.setHand(player1, List.of(new Anathemancer()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Anathemancer").getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Anathemancer");
        harness.assertLife(player2, 19);
    }

    @Test
    void unearthReturnsOnlyTheActivatedCard() {
        Anathemancer first = new Anathemancer();
        Anathemancer second = new Anathemancer();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Anathemancer").getCard().getId()).isEqualTo(first.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
        assertThat(countPermanents(player1, "Anathemancer")).isEqualTo(1);
    }

    @Test
    void unearthCannotBeActivatedDuringOpponentsMainPhase() {
        prepareUnearth();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Anathemancer");
        harness.assertNotOnBattlefield(player1, "Anathemancer");
    }

    @Test
    void unearthCannotBeActivatedWhileAnotherUnearthAbilityIsOnStack() {
        prepareUnearth();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Anathemancer");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Anathemancer");
        assertThat(countPermanents(player1, "Anathemancer")).isEqualTo(1);
    }

    private void prepareUnearth() {
        harness.setGraveyard(player1, List.of(new Anathemancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void unearthTargetingOpponent() {
        prepareUnearth();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
    }
}
