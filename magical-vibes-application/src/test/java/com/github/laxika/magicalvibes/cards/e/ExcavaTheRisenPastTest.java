package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImpactTremors;
import com.github.laxika.magicalvibes.cards.c.CharcoalDiamond;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.cards.t.ThunderingGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExcavaTheRisenPast.class, CharcoalDiamond.class, GrizzlyBears.class,
        Pacifism.class, ThunderingGiant.class, ImpactTremors.class, Solemnity.class})
class ExcavaTheRisenPastTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an eligible card as a 1/1 Spirit with flying and a finality counter")
    void returnsEligibleCardAsSpirit() {
        Card artifact = new CharcoalDiamond();
        harness.setGraveyard(player1, List.of(artifact));
        addCreatureReady(player1, new ExcavaTheRisenPast());

        declareAttackers(player1, List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, artifact.getName());
        assertThat(gqs.isCreature(gd, returned)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, returned)).contains(CardSubtype.SPIRIT);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only artifact, creature, and non-Aura enchantment cards with mana value 3 or less are valid")
    void filtersGraveyardTargets() {
        Card artifact = new CharcoalDiamond();
        Card creature = new GrizzlyBears();
        Card aura = new Pacifism();
        Card tooExpensive = new ThunderingGiant();
        harness.setGraveyard(player1, List.of(artifact, creature, aura, tooExpensive));
        addCreatureReady(player1, new ExcavaTheRisenPast());

        declareAttackers(player1, List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(artifact.getId(), creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Charcoal Diamond");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Pacifism");
        harness.assertInGraveyard(player1, "Thundering Giant");
    }

    @Test
    @DisplayName("Returned creatures retain their original subtypes and become 1/1 Spirits")
    void returnsCreatureWithOriginalSubtype() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addCreatureReady(player1, new ExcavaTheRisenPast());

        declareAttackers(player1, List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, creature.getName());
        assertThat(gqs.effectiveCreatureSubtypes(gd, returned))
                .contains(CardSubtype.BEAR, CardSubtype.SPIRIT);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(returned.isAttacking()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("A returned artifact enters as a creature and triggers Impact Tremors")
    void returnedArtifactTriggersCreatureEntryAbility() {
        Card artifact = new CharcoalDiamond();
        harness.setGraveyard(player1, List.of(artifact));
        addCreatureReady(player1, new ExcavaTheRisenPast());
        harness.addToBattlefield(player1, new ImpactTremors());

        declareAttackers(player1, List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertLife(player2, 19);
        Permanent returned = findPermanent(player1, artifact.getName());
        assertThat(gqs.isArtifact(gd, returned)).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isTrue();
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A non-Aura enchantment returns as a 1/1 Spirit enchantment creature")
    void returnsNonAuraEnchantmentAsCreature() {
        Card enchantment = new ImpactTremors();
        harness.setGraveyard(player1, List.of(enchantment));
        addCreatureReady(player1, new ExcavaTheRisenPast());

        declareAttackers(player1, List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        Permanent returned = findPermanent(player1, enchantment.getName());
        assertThat(gqs.isEnchantment(gd, returned)).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, returned)).contains(CardSubtype.SPIRIT);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    @DisplayName("The animation and finality counter continue to work after Excava leaves")
    void animationAndFinalityPersistWithoutExcava() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        Permanent excava = addCreatureReady(player1, new ExcavaTheRisenPast());

        declareAttackers(player1, List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, excava);

        Permanent returned = findPermanent(player1, creature.getName());
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, returned);

        harness.assertNotOnBattlefield(player1, creature.getName());
        harness.assertNotInGraveyard(player1, creature.getName());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Solemnity prevents the finality counter without preventing the return")
    void solemnityPreventsFinalityCounter() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addCreatureReady(player1, new ExcavaTheRisenPast());
        harness.addToBattlefield(player1, new Solemnity());

        declareAttackers(player1, List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, creature.getName());
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isZero();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(1);
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, returned);
        harness.assertInGraveyard(player1, creature.getName());
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("A target that leaves the graveyard before resolution is not returned")
    void missingTargetIsNotReturned() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addCreatureReady(player1, new ExcavaTheRisenPast());

        declareAttackers(player1, List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(creature);
        gd.addToExile(player1.getId(), creature);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, creature.getName());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
    }
}
