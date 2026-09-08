package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistformSliver.class, GrizzlyBears.class})
class MistformSliverTest extends BaseCardTest {

    @Test
    void noncreatureSliverCanGainAnotherCreatureType() {
        harness.addToBattlefield(player1, new MistformSliver());
        Card kindredArtifact = new Card();
        kindredArtifact.setName("Kindred Sliver Artifact");
        kindredArtifact.setType(CardType.ARTIFACT);
        kindredArtifact.setAdditionalTypes(Set.of(CardType.KINDRED));
        kindredArtifact.setSubtypes(List.of(CardSubtype.SLIVER));
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, kindredArtifact);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.ELF.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, sliver)).contains(CardSubtype.SLIVER, CardSubtype.ELF);
        assertThat(gqs.isCreature(gd, sliver)).isFalse();
    }

    @Test
    @DisplayName("All Slivers gain the ability, including opposing Slivers and itself")
    void grantsAbilityToAllSlivers() {
        Permanent source = addCreatureReady(player1, new MistformSliver());
        Permanent ownSliver = addCreatureReady(player1, new MistformSliver());
        Permanent opposingSliver = addCreatureReady(player2, new MistformSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gs.getEffectiveActivatedAbilities(gd, source)).isNotEmpty();
        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).isNotEmpty();
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingSliver)).isNotEmpty();
        assertThat(gs.getEffectiveActivatedAbilities(gd, bears)).isEmpty();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(bears), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Sliver can become the chosen creature type until end of turn")
    void sliverBecomesChosenCreatureTypeUntilEndOfTurn() {
        Permanent sliver = addCreatureReady(player1, new MistformSliver());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, sliver))
                .containsExactlyInAnyOrder(CardSubtype.ILLUSION, CardSubtype.SLIVER, CardSubtype.GOBLIN);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.ELF.name());
        assertThat(gqs.effectiveCreatureSubtypes(gd, sliver))
                .contains(CardSubtype.GOBLIN, CardSubtype.ELF, CardSubtype.SLIVER);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, sliver))
                .containsExactlyInAnyOrder(CardSubtype.ILLUSION, CardSubtype.SLIVER);
    }
}
