package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GuardianOfTheAges;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({UrsineMonstrosity.class, Forest.class, Millstone.class, Shock.class, GuardianOfTheAges.class})
class UrsineMonstrosityTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, Ursine Monstrosity mills and attacks a random opponent")
    void beginningOfCombatAbility() {
        Permanent ursine = addCreatureReady(player1, new UrsineMonstrosity());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock()));
        harness.setLibrary(player1, List.of(new Millstone()));

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(ursine.isMustAttackThisCombat()).isTrue();
        assertThat(ursine.getMustAttackTargetId()).isEqualTo(player2.getId());
        assertThat(gqs.getEffectivePower(gd, ursine)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ursine)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, ursine, Keyword.INDESTRUCTIBLE)).isTrue();

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("The beginning-of-combat ability does not trigger on an opponent's turn")
    void doesNotTriggerOnOpponentsCombat() {
        Permanent ursine = addCreatureReady(player1, new UrsineMonstrosity());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock()));
        harness.setLibrary(player1, List.of(new Millstone()));

        advanceToBeginningOfCombat(player2);
        harness.passBothPriorities();

        assertThat(ursine.isMustAttackThisCombat()).isFalse();
        assertThat(gqs.getEffectivePower(gd, ursine)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ursine, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The temporary boost and indestructible wear off at end of turn")
    void temporaryEffectsWearOff() {
        Permanent ursine = addCreatureReady(player1, new UrsineMonstrosity());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock()));
        harness.setLibrary(player1, List.of(new Millstone()));

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ursine)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, ursine, Keyword.INDESTRUCTIBLE)).isTrue();

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, ursine)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ursine, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("An empty library does not prevent indestructible or the attack requirement")
    void emptyLibraryStillResolvesRemainingInstructions() {
        Permanent ursine = addCreatureReady(player1, new UrsineMonstrosity());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of());

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ursine)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ursine)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ursine, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Count distinct types, including every type of a multitype card, only in your graveyard")
    void countsDistinctTypesAfterMillingAndLocksInBoost() {
        Permanent ursine = addCreatureReady(player1, new UrsineMonstrosity());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Millstone()));
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new GuardianOfTheAges(), new Shock()));

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Guardian of the Ages");
        assertThat(gqs.getEffectivePower(gd, ursine)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ursine)).isEqualTo(6);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, ursine)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ursine)).isEqualTo(6);

        harness.setGraveyard(player1, List.of(new Forest(), new GuardianOfTheAges(), new Shock()));
        assertThat(gqs.getEffectivePower(gd, ursine)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ursine)).isEqualTo(6);
    }

    @Test
    @DisplayName("A tapped creature still gets the bonus but need not attack")
    void tappedCreatureIsNotForcedToAttack() {
        Permanent ursine = addCreatureReady(player1, new UrsineMonstrosity());
        ursine.tap();
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ursine)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ursine, Keyword.INDESTRUCTIBLE)).isTrue();
        declareAttackers(player1, List.of());
        assertThat(ursine.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick creature still resolves the ability but need not attack")
    void summoningSickCreatureIsNotForcedToAttack() {
        Permanent ursine = harness.addToBattlefieldAndReturn(player1, new UrsineMonstrosity());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ursine)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ursine, Keyword.INDESTRUCTIBLE)).isTrue();
        declareAttackers(player1, List.of());
        assertThat(ursine.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("The chosen attack destination expires with combat while the bonus lasts until cleanup")
    void attackDestinationExpiresAtEndOfCombat() {
        Permanent ursine = addCreatureReady(player1, new UrsineMonstrosity());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat(player1);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, ursine)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ursine, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(ursine.isMustAttackThisCombat()).isFalse();
        assertThat(ursine.getMustAttackTargetId()).isNull();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

}
