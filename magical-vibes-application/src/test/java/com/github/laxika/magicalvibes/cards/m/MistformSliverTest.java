package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.p.PlatedSliver;
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

@CardUsed({MistformSliver.class, FugitiveWizard.class, PlatedSliver.class})
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
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());

        assertThat(gs.getEffectiveActivatedAbilities(gd, source)).hasSize(3);
        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).hasSize(3);
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingSliver)).hasSize(3);
        assertThat(gs.getEffectiveActivatedAbilities(gd, wizard)).isEmpty();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(wizard), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opposing Sliver can activate Mistform Sliver's granted ability")
    void opposingSliverCanActivateAbility() {
        addCreatureReady(player1, new MistformSliver());
        Permanent opposingSliver = addCreatureReady(player2, new MistformSliver());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player2, CardSubtype.ELF.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, opposingSliver))
                .contains(CardSubtype.ILLUSION, CardSubtype.SLIVER, CardSubtype.ELF);
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

    @Test
    @CardUsed(AmoeboidChangeling.class)
    @DisplayName("A Sliver that loses all creature types no longer has Mistform Sliver's ability")
    void losingSliverTypeRemovesAbility() {
        Permanent mistformSliver = addCreatureReady(player1, new MistformSliver());
        Permanent amoeboid = addCreatureReady(player1, new AmoeboidChangeling());

        assertThat(gs.getEffectiveActivatedAbilities(gd, mistformSliver)).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(amoeboid), 1, null, mistformSliver.getId());
        harness.passBothPriorities();

        assertThat(gs.getEffectiveActivatedAbilities(gd, mistformSliver)).isEmpty();
    }

    @Test
    @DisplayName("Slivers lose Mistform Sliver's granted ability when it leaves the battlefield")
    void losesGrantedAbilityWhenSourceLeaves() {
        Permanent source = addCreatureReady(player1, new MistformSliver());
        Permanent sliver = addCreatureReady(player2, new PlatedSliver());

        assertThat(gs.getEffectiveActivatedAbilities(gd, sliver)).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(source);

        assertThat(gs.getEffectiveActivatedAbilities(gd, sliver)).isEmpty();
    }
}
