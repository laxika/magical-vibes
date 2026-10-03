package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DinoDNA.class, GrizzlyBears.class, Ornithopter.class, AvianChangeling.class})
class DinoDNATest extends BaseCardTest {

    @Test
    void imprintsTargetCreatureFromAnyGraveyard() {
        DinoDNA dna = new DinoDNA();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, dna);
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(permanent.getId())).containsExactly(creature);
        assertThat(gd.getImprintedCard(dna)).isSameAs(creature);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void createsTargetedGreenDinosaurTokenCopy() {
        DinoDNA dna = new DinoDNA();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, dna);
        GrizzlyBears creature = new GrizzlyBears();
        gd.addToExile(player1.getId(), creature, permanent.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, creature.getId(), Zone.EXILE);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(candidate -> candidate.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DINOSAUR);
        assertThat(token.getCard().getPower()).isEqualTo(6);
        assertThat(token.getCard().getToughness()).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void cannotTargetCreatureNotExiledWithThisArtifact() {
        harness.addToBattlefieldAndReturn(player1, new DinoDNA());
        GrizzlyBears creature = new GrizzlyBears();
        harness.setExile(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, creature.getId(), Zone.EXILE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void artifactCreatureCopyIsOnlyACreatureAndRetainsFlying() {
        Permanent dna = harness.addToBattlefieldAndReturn(player1, new DinoDNA());
        Ornithopter creature = new Ornithopter();
        gd.addToExile(player2.getId(), creature, dna.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, creature.getId(), Zone.EXILE);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(candidate -> candidate.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isFalse();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DINOSAUR);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
        assertThat(token.getCard().getColors()).containsExactly(CardColor.GREEN);
        assertThat(token.getCard().getPower()).isEqualTo(6);
        assertThat(token.getCard().getToughness()).isEqualTo(6);
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void dinosaurSubtypeOverrideDoesNotCopyChangeling() {
        Permanent dna = harness.addToBattlefieldAndReturn(player1, new DinoDNA());
        AvianChangeling creature = new AvianChangeling();
        gd.addToExile(player1.getId(), creature, dna.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, creature.getId(), Zone.EXILE);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(candidate -> candidate.getCard().isToken()).findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, token, Keyword.CHANGELING)).isFalse();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DINOSAUR);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void bothAbilitiesRequireMainPhaseTiming() {
        Permanent dna = harness.addToBattlefieldAndReturn(player1, new DinoDNA());
        GrizzlyBears graveyardCreature = new GrizzlyBears();
        GrizzlyBears exiledCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        gd.addToExile(player1.getId(), exiledCreature, dna.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, graveyardCreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, exiledCreature.getId(), Zone.EXILE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotImprintNoncreatureCard() {
        harness.addToBattlefield(player1, new DinoDNA());
        DinoDNA artifact = new DinoDNA();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, artifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothAbilitiesRequireEmptyStack() {
        Permanent firstDna = harness.addToBattlefieldAndReturn(player1, new DinoDNA());
        harness.addToBattlefield(player1, new DinoDNA());
        GrizzlyBears graveyardCreature = new GrizzlyBears();
        GrizzlyBears exiledCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        gd.addToExile(player1.getId(), exiledCreature, firstDna.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, 0, null, graveyardCreature.getId(), Zone.GRAVEYARD);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 1, 0, null, graveyardCreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, exiledCreature.getId(), Zone.EXILE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void imprintDoesNothingIfTargetLeavesGraveyardBeforeResolution() {
        Permanent dna = harness.addToBattlefieldAndReturn(player1, new DinoDNA());
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(creature));

        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(dna.getId())).isEmpty();
        assertThat(dna.isTapped()).isTrue();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void anotherDinoDnaCannotCopyCardsExiledByFirstArtifact() {
        Permanent firstDna = harness.addToBattlefieldAndReturn(player1, new DinoDNA());
        harness.addToBattlefield(player1, new DinoDNA());
        GrizzlyBears creature = new GrizzlyBears();
        gd.addToExile(player1.getId(), creature, firstDna.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 1, 1, null, creature.getId(), Zone.EXILE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canCopyEarlierImprintAfterImprintingAnotherCreature() {
        Permanent dna = harness.addToBattlefieldAndReturn(player1, new DinoDNA());
        GrizzlyBears first = new GrizzlyBears();
        Ornithopter second = new Ornithopter();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, first.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        assertThat(dna.isTapped()).isTrue();
        dna.setTapped(false);
        harness.activateAbility(player1, 0, 0, null, second.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, first.getId(), Zone.EXILE);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(dna.getId())).containsExactly(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears"));
    }

    @Test
    void missingExiledTargetDoesNotFallBackToAnotherImprint() {
        Permanent dna = harness.addToBattlefieldAndReturn(player1, new DinoDNA());
        GrizzlyBears target = new GrizzlyBears();
        GrizzlyBears other = new GrizzlyBears();
        gd.addToExile(player1.getId(), target, dna.getId());
        gd.addToExile(player1.getId(), other, dna.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.EXILE);
        gd.removeFromExile(target.getId());
        harness.setGraveyard(player1, List.of(target));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(dna);
        assertThat(gd.findExiledCard(other.getId())).isNotNull();
    }

    @Test
    void tokenAbilityResolvesAfterSourceLeavesBattlefield() {
        Permanent dna = harness.addToBattlefieldAndReturn(player1, new DinoDNA());
        GrizzlyBears creature = new GrizzlyBears();
        gd.addToExile(player1.getId(), creature, dna.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, 1, null, creature.getId(), Zone.EXILE);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dna));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(token -> assertThat(token.getCard().isToken()).isTrue());
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
    }
}
