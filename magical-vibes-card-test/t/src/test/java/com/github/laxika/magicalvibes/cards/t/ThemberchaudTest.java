package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Themberchaud.class, Mountain.class, AirElemental.class, GiantSpider.class})
class ThemberchaudTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals damage based on Mountains to each player and other nonflying creature")
    void entersAndDamagesNonflyingCreaturesAndPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent flyingCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        Permanent themberchaud = castThemberchaud();
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        assertThat(groundCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(flyingCreature.getMarkedDamage()).isZero();
        assertThat(themberchaud.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addReadyThemberchaud();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Exerting gives flying until end of turn and skips the next untap")
    void exertGivesFlyingAndSkipsUntap() {
        Permanent themberchaud = addReadyThemberchaud();

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, themberchaud, Keyword.FLYING)).isTrue();
        assertThat(themberchaud.isTapped()).isTrue();
        assertThat(themberchaud.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert does not give flying or skip the next untap")
    void decliningExertDoesNothing() {
        Permanent themberchaud = addReadyThemberchaud();

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasKeyword(gd, themberchaud, Keyword.FLYING)).isFalse();
        assertThat(themberchaud.getSkipUntapCount()).isZero();
    }

    @Test
    void noMountainsMeansNoDamageEvenIfOpponentControlsMountains() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Mountain());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        Permanent themberchaud = castThemberchaud();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(themberchaud.getMarkedDamage()).isZero();
    }

    @Test
    void countsMountainsAtResolutionAndDamagesFriendlyCreaturesToo() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        Permanent friendlyCreature = harness.addToBattlefieldAndReturn(player1, new GiantSpider());

        Permanent themberchaud = castThemberchaud();
        harness.addToBattlefield(player1, new Mountain());
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        assertThat(friendlyCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(themberchaud.getMarkedDamage()).isZero();
    }

    @Test
    void exertRestrictionAppliesBeforeFlyingTriggerResolves() {
        Permanent themberchaud = addReadyThemberchaud();

        declareAttackers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(themberchaud.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.permanentsExertedThisTurn).contains(themberchaud.getId());
        assertThat(gqs.hasKeyword(gd, themberchaud, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, themberchaud, Keyword.FLYING)).isTrue();
    }

    @Test
    void flyingExpiresAndExertSkipsOnlyNextControllerUntap() {
        Permanent themberchaud = addReadyThemberchaud();
        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, themberchaud, Keyword.FLYING)).isFalse();
        assertThat(themberchaud.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(themberchaud.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(themberchaud.isTapped()).isFalse();
    }

    private Permanent castThemberchaud() {
        harness.castFromHand(player1, new Themberchaud(), "{4}{R}{R}{R}");
        harness.withAutoStop(gd.currentStep, () -> harness.passBothPriorities());
        return findPermanent(player1, "Themberchaud");
    }

    private Permanent addReadyThemberchaud() {
        return addCreatureReady(player1, new Themberchaud());
    }
}
