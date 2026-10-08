package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FieldmistBorderpost;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VectisDominator.class, FieldmistBorderpost.class})
class VectisDominatorTest extends BaseCardTest {

    @Test
    @DisplayName("Controller pays 2 life to keep the creature untapped")
    void controllerPaysLifeCreatureStaysUntapped() {
        addCreatureReady(player1, new VectisDominator());
        Permanent target = addCreatureReady(player2, new VectisDominator());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 18);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Controller declines and the creature is tapped")
    void controllerDeclinesCreatureTapped() {
        addCreatureReady(player1, new VectisDominator());
        Permanent target = addCreatureReady(player2, new VectisDominator());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A controller with too little life can't pay and the creature is tapped automatically")
    void cannotPayTapsAutomatically() {
        addCreatureReady(player1, new VectisDominator());
        Permanent target = addCreatureReady(player2, new VectisDominator());
        harness.setLife(player2, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // No choice offered — the controller can't pay 2 life, so the creature is tapped outright.
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 1);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability fizzles if the target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        addCreatureReady(player1, new VectisDominator());
        Permanent target = addCreatureReady(player2, new VectisDominator());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new VectisDominator());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FieldmistBorderpost());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetFriendlyCreature() {
        addCreatureReady(player1, new VectisDominator());
        Permanent target = addCreatureReady(player1, new VectisDominator());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 18);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void tappedTargetStillOffersLifePayment() {
        addCreatureReady(player1, new VectisDominator());
        Permanent target = addCreatureReady(player2, new VectisDominator());
        target.tap();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 18);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new VectisDominator());
        Permanent target = addCreatureReady(player2, new VectisDominator());

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(source.isTapped()).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void summoningSickSourceCannotActivate() {
        harness.addToBattlefield(player1, new VectisDominator());
        Permanent target = addCreatureReady(player2, new VectisDominator());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tappedSourceCannotActivate() {
        Permanent source = addCreatureReady(player1, new VectisDominator());
        source.tap();
        Permanent target = addCreatureReady(player2, new VectisDominator());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
