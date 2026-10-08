package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.a.AttentiveSunscribe;
import com.github.laxika.magicalvibes.cards.c.Confiscate;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MalametVeteran;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({ZoeticGlyph.class, FountainOfYouth.class, Disenchant.class, GrizzlyBears.class, Plains.class,
        AttentiveSunscribe.class, Confiscate.class, MalametVeteran.class})
class ZoeticGlyphTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted artifact becomes a 5/4 Golem creature and remains an artifact")
    void animatesEnchantedArtifact() {
        Permanent artifact = castOnArtifact();

        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, artifact)).contains(CardSubtype.GOLEM);
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(4);
    }

    @Test
    @DisplayName("Discovers 3 when Zoetic Glyph is put into a graveyard from the battlefield")
    void discoversWhenPutIntoGraveyardFromBattlefield() {
        GrizzlyBears discovered = new GrizzlyBears();
        Plains land = new Plains();
        harness.setLibrary(player1, List.of(land, discovered));
        castOnArtifact();
        Permanent aura = findPermanent(player1, "Zoetic Glyph");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castInstant(player2, 0, aura.getId());
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
    }

    @Test
    @DisplayName("Can enchant only an artifact")
    void cannotEnchantNonArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ZoeticGlyph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    void preservesExistingCreatureSubtypeAndAddsCountersAboveBasePowerToughness() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AttentiveSunscribe());
        artifact.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new ZoeticGlyph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, artifact))
                .contains(CardSubtype.GNOME, CardSubtype.GOLEM);
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(5);
    }

    @Test
    void discoversWhenEnchantedArtifactIsDestroyed() {
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(discovered));
        Permanent artifact = castOnArtifact();

        destroyWithDisenchant(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof ZoeticGlyph);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(artifact.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class))
                .isNotNull();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
    }

    @Test
    void canCastDiscoveredCardForFreeAndBottomsSkippedCards() {
        Plains land = new Plains();
        MalametVeteran expensive = new MalametVeteran();
        GrizzlyBears discovered = new GrizzlyBears();
        Plains remaining = new Plains();
        harness.setLibrary(player1, List.of(land, expensive, discovered, remaining));
        castOnArtifact();

        destroyWithDisenchant(findPermanent(player1, "Zoetic Glyph"));
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Grizzly Bears").getCard()).isSameAs(discovered);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discovered);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(remaining);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(land, expensive);
        assertThat(gd.findExiledCard(land.getId())).isNull();
        assertThat(gd.findExiledCard(expensive.getId())).isNull();
        assertThat(gd.findExiledCard(discovered.getId())).isNull();
    }

    @Test
    void returnsEntireLibraryToBottomWhenNothingQualifies() {
        Plains land = new Plains();
        MalametVeteran expensive = new MalametVeteran();
        harness.setLibrary(player1, List.of(land, expensive));
        castOnArtifact();

        destroyWithDisenchant(findPermanent(player1, "Zoetic Glyph"));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, expensive);
        assertThat(gd.findExiledCard(land.getId())).isNull();
        assertThat(gd.findExiledCard(expensive.getId())).isNull();
    }

    @Test
    void stopsAnimatingArtifactWhenAuraIsDestroyed() {
        harness.setLibrary(player1, List.of());
        Permanent artifact = castOnArtifact();

        destroyWithDisenchant(findPermanent(player1, "Zoetic Glyph"));

        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, artifact)).doesNotContain(CardSubtype.GOLEM);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void discoverIncludesCardWithManaValueExactlyThree() {
        ZoeticGlyph discovered = new ZoeticGlyph();
        harness.setLibrary(player1, List.of(discovered));
        castOnArtifact();

        destroyWithDisenchant(findPermanent(player1, "Zoetic Glyph"));
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
    }

    @Test
    void lastControllerDiscoversWhenAuraWasStolen() {
        GrizzlyBears ownersCard = new GrizzlyBears();
        GrizzlyBears controllersCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownersCard));
        harness.setLibrary(player2, List.of(controllersCard));
        castOnArtifact();
        Permanent aura = findPermanent(player1, "Zoetic Glyph");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Confiscate()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player2, 0, aura.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player2, "Zoetic Glyph")).isSameAs(aura);

        destroyWithDisenchant(aura);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(controllersCard);
        harness.handleCardChosen(player2, -1);

        assertThat(gd.playerHands.get(player2.getId())).contains(controllersCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownersCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura.getCard());
    }

    @Test
    void doesNotDiscoverWhenTargetIsDestroyedBeforeAuraResolves() {
        GrizzlyBears libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ZoeticGlyph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, artifact.getId());
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castInstant(player2, 0, artifact.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof ZoeticGlyph);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void destroyWithDisenchant(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();
    }

    private Permanent castOnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ZoeticGlyph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();
        return artifact;
    }
}
