package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.r.RendFlesh;
import com.github.laxika.magicalvibes.cards.s.SenseisDiviningTop;
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

@CardUsed({HisokasGuard.class, LanternKami.class, DevotedRetainer.class, RendFlesh.class,
        SenseisDiviningTop.class})
class HisokasGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives the target creature shroud")
    void resolvingGrantsShroud() {
        addCreatureReady(player1, new HisokasGuard());
        Permanent bear = addCreatureReady(player1, new LanternKami());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Shroud persists past end of turn while the Guard stays tapped")
    void shroudSurvivesEndOfTurnWhileTapped() {
        addCreatureReady(player1, new HisokasGuard());
        Permanent bear = addCreatureReady(player1, new LanternKami());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Shroud ends when the Guard becomes untapped")
    void shroudEndsWhenGuardUntaps() {
        Permanent guard = addCreatureReady(player1, new HisokasGuard());
        Permanent bear = addCreatureReady(player1, new LanternKami());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.SHROUD)).isTrue();

        advanceToNextTurnWithMayChoice(player2, true);
        assertThat(guard.isTapped()).isFalse();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud persists when the controller keeps the Guard tapped")
    void shroudPersistsWhenKeptTapped() {
        Permanent guard = addCreatureReady(player1, new HisokasGuard());
        Permanent bear = addCreatureReady(player1, new LanternKami());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        advanceToNextTurnWithMayChoice(player2, false);
        assertThat(guard.isTapped()).isTrue();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Shroud ends when the Guard leaves the battlefield")
    void shroudEndsWhenGuardRemoved() {
        Permanent guard = addCreatureReady(player1, new HisokasGuard());
        Permanent bear = addCreatureReady(player1, new LanternKami());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.SHROUD)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, guard));

        assertThat(gqs.hasKeyword(gd, bear, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Cannot target the Guard itself")
    void cannotTargetItself() {
        Permanent guard = addCreatureReady(player1, new HisokasGuard());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, guard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("other than this creature");
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addCreatureReady(player1, new HisokasGuard());
        Permanent enemyBear = addCreatureReady(player2, new LanternKami());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enemyBear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new HisokasGuard());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SenseisDiviningTop());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control other than this creature");
    }

    @Test
    @DisplayName("Shroud prevents a targeted spell from targeting the protected creature")
    void shroudPreventsTargetedSpell() {
        addCreatureReady(player1, new HisokasGuard());
        Permanent retainer = addCreatureReady(player1, new DevotedRetainer());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, retainer.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new RendFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, retainer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("has shroud and can't be targeted");
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(newActivePlayer, TurnStep.UNTAP);

        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
