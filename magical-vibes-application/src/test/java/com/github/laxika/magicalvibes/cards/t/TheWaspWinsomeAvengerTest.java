package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CaptainAmericaTeamLeader;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheWaspWinsomeAvenger.class, CaptainAmericaTeamLeader.class, GrizzlyBears.class})
class TheWaspWinsomeAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB grants a target Hero hexproof until end of turn")
    void etbGrantsHeroHexproof() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new CaptainAmericaTeamLeader());

        castWasp(hero.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, hero, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hero, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("ETB cannot target a non-Hero creature")
    void etbRejectsNonHeroTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new TheWaspWinsomeAvenger()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Hero");
    }

    @Test
    @DisplayName("Attacking taps a target creature defending player controls")
    void attackTriggerTapsDefendingCreature() {
        Permanent attacker = addCreatureReady(player1, new TheWaspWinsomeAvenger());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds())
                .containsExactly(defendingCreature.getId())
                .doesNotContain(attacker.getId(), ownCreature.getId());

        harness.handlePermanentChosen(player1, defendingCreature.getId());
        harness.passBothPriorities();

        assertThat(defendingCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB can grant hexproof to an opposing Hero")
    void etbCanTargetOpposingHero() {
        Permanent hero = harness.addToBattlefieldAndReturn(player2, new CaptainAmericaTeamLeader());

        castWasp(hero.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, hero, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("The Wasp can target itself with its ETB ability")
    void etbCanTargetItself() {
        Permanent wasp = harness.enterBattlefieldAndReturn(player1, new TheWaspWinsomeAvenger());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).contains(wasp.getId());
        harness.handlePermanentChosen(player1, wasp.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, wasp, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Attack trigger resolves even after The Wasp leaves the battlefield")
    void attackTriggerSurvivesSourceLeaving() {
        Permanent attacker = addCreatureReady(player1, new TheWaspWinsomeAvenger());
        Permanent defender = addCreatureReady(player2, new CaptainAmericaTeamLeader());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, defender.getId());
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        resolveAllTriggers();

        assertThat(defender.isTapped()).isTrue();
    }

    private void castWasp(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new TheWaspWinsomeAvenger()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
