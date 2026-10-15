package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.i.InscribedTablet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YavimayaSteelcrusher.class, InscribedTablet.class})
class YavimayaSteelcrusherTest extends BaseCardTest {

    @Test
    @DisplayName("Enlist taps a nonattacking creature and boosts Yavimaya Steelcrusher by its power")
    void enlistBoostsAttackerBySupporterPower() {
        Permanent crusher = addCreatureReady(player1, new YavimayaSteelcrusher());
        Permanent supporter = addCreatureReady(player1, new YavimayaSteelcrusher());

        declareAttackers(List.of(0));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(supporter.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passBothPriorities();

        assertThat(supporter.isTapped()).isTrue();
        assertThat(crusher.getPowerModifier()).isEqualTo(2);
        assertThat(crusher.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Sacrificing Yavimaya Steelcrusher destroys a target artifact")
    void sacrificesAndDestroysArtifact() {
        addCreatureReady(player1, new YavimayaSteelcrusher());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new InscribedTablet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Yavimaya Steelcrusher");
        harness.assertInGraveyard(player2, "Inscribed Tablet");
    }

    @Test
    @DisplayName("The activated ability cannot target a nonartifact permanent")
    void rejectsNonartifactTarget() {
        addCreatureReady(player1, new YavimayaSteelcrusher());
        Permanent creature = addCreatureReady(player2, new YavimayaSteelcrusher());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enlistUsesLastKnownPowerWhenSupporterIsSacrificedInResponse() {
        Permanent crusher = addCreatureReady(player1, new YavimayaSteelcrusher());
        Permanent supporter = addCreatureReady(player1, new YavimayaSteelcrusher());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new InscribedTablet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        assertThat(crusher.getPowerModifier()).isZero();

        harness.activateAbility(player1, 1, null, artifact.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(supporter);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Inscribed Tablet");
        assertThat(crusher.getPowerModifier()).isEqualTo(2);
        assertThat(crusher.getToughnessModifier()).isZero();
    }

    @Test
    void canDeclineEnlist() {
        Permanent crusher = addCreatureReady(player1, new YavimayaSteelcrusher());
        Permanent supporter = addCreatureReady(player1, new YavimayaSteelcrusher());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(supporter.isTapped()).isFalse();
        assertThat(crusher.getPowerModifier()).isZero();
    }

    @Test
    void enlistExcludesTappedSummoningSickAndOpposingCreatures() {
        addCreatureReady(player1, new YavimayaSteelcrusher());
        Permanent eligible = addCreatureReady(player1, new YavimayaSteelcrusher());
        Permanent tapped = addCreatureReady(player1, new YavimayaSteelcrusher());
        tapped.tap();
        harness.addToBattlefield(player1, new YavimayaSteelcrusher());
        addCreatureReady(player2, new YavimayaSteelcrusher());

        declareAttackers(List.of(0));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(eligible.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());
    }

    @Test
    void canSacrificeWhileTappedAndSummoningSick() {
        Permanent crusher = harness.addToBattlefieldAndReturn(player1, new YavimayaSteelcrusher());
        crusher.tap();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new InscribedTablet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, artifact.getId());

        harness.assertNotOnBattlefield(player1, "Yavimaya Steelcrusher");
        harness.assertInGraveyard(player1, "Yavimaya Steelcrusher");
        harness.assertOnBattlefield(player2, "Inscribed Tablet");
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Inscribed Tablet");
    }

    @Test
    void canDestroyOwnArtifact() {
        addCreatureReady(player1, new YavimayaSteelcrusher());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new InscribedTablet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Yavimaya Steelcrusher");
        harness.assertInGraveyard(player1, "Inscribed Tablet");
    }

    @Test
    void cannotActivateWithoutManaAndDoesNotSacrifice() {
        addCreatureReady(player1, new YavimayaSteelcrusher());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new InscribedTablet());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Yavimaya Steelcrusher");
        harness.assertNotInGraveyard(player1, "Yavimaya Steelcrusher");
        harness.assertOnBattlefield(player2, "Inscribed Tablet");
    }
}
