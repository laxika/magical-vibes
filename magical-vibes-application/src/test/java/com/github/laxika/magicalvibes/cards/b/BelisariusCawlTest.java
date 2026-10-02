package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IzzetCluestone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BelisariusCawl.class, Ornithopter.class, GrizzlyBears.class, IzzetCluestone.class, Plains.class})
class BelisariusCawlTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping two artifacts creates a vigilant Astartes Warrior")
    void tappingArtifactsCreatesAstartesWarrior() {
        Permanent cawl = addCreatureReady(player1, new BelisariusCawl());
        Permanent artifact1 = addCreatureReady(player1, new Ornithopter());
        Permanent artifact2 = addCreatureReady(player1, new Ornithopter());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cawl), null, null);
        harness.passBothPriorities();

        assertThat(cawl.isTapped()).isTrue();
        assertThat(artifact1.isTapped()).isTrue();
        assertThat(artifact2.isTapped()).isTrue();
        assertThat(creature.isTapped()).isFalse();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Astartes Warrior");
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ASTARTES, CardSubtype.WARRIOR);
        assertThat(token.getCard().getKeywords()).contains(Keyword.VIGILANCE);
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping X creatures looks at X cards and offers an artifact")
    void tappingCreaturesLooksAtXCardsAndOffersArtifact() {
        Permanent cawl = addCreatureReady(player1, new BelisariusCawl());
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());
        Card nonArtifact = new GrizzlyBears();
        Card artifact = new IzzetCluestone();
        Card land = new Plains();
        harness.setLibrary(player1, List.of(nonArtifact, artifact, land));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cawl), 1, 2, null);

        assertThat(cawl.isTapped()).isTrue();
        assertThat(creature1.isTapped()).isTrue();
        assertThat(creature2.isTapped()).isTrue();

        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(nonArtifact, artifact);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The creature-tapping ability excludes Belisarius Cawl from X")
    void creatureTappingCostExcludesSource() {
        Permanent cawl = addCreatureReady(player1, new BelisariusCawl());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cawl), 1, 1, null);

        assertThat(cawl.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void mayDeclineArtifactAndBottomAllLookedAtCards() {
        Permanent cawl = addCreatureReady(player1, new BelisariusCawl());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Card artifact = new IzzetCluestone();
        Card creature = new GrizzlyBears();
        Card untouched = new Plains();
        harness.setLibrary(player1, List.of(artifact, creature, untouched));

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(cawl.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(artifact, creature);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(artifact, creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void zeroXRequiresOnlyTappingCawlAndLeavesLibraryUnchanged() {
        Permanent cawl = addCreatureReady(player1, new BelisariusCawl());
        Card top = new Plains();
        harness.setLibrary(player1, List.of(top));

        harness.activateAbility(player1, 0, 1, 0, null);
        harness.passBothPriorities();

        assertThat(cawl.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noEligibleArtifactBottomsLookedAtCardsWithoutChoice() {
        addCreatureReady(player1, new BelisariusCawl());
        addCreatureReady(player1, new GrizzlyBears());
        Card lookedAt = new Plains();
        Card untouched = new Plains();
        harness.setLibrary(player1, List.of(lookedAt, untouched));

        harness.activateAbility(player1, 0, 1, 1, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, lookedAt);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(lookedAt);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void summoningSickCreaturesMayPayAdditionalTapCost() {
        addCreatureReady(player1, new BelisariusCawl());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(true);
        Card top = new Plains();
        harness.setLibrary(player1, List.of(top));

        harness.activateAbility(player1, 0, 1, 1, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cawlCannotCountAsOneOfTwoAdditionalArtifacts() {
        Permanent cawl = addCreatureReady(player1, new BelisariusCawl());
        Permanent artifact = addCreatureReady(player1, new Ornithopter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cawl.isTapped()).isFalse();
        assertThat(artifact.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void looksAtAvailableCardsWhenLibraryHasFewerThanX() {
        addCreatureReady(player1, new BelisariusCawl());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Card artifactCreature = new Ornithopter();
        harness.setLibrary(player1, List.of(artifactCreature));

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(artifactCreature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(artifactCreature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotTakeTwoArtifactsFromOneActivation() {
        addCreatureReady(player1, new BelisariusCawl());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Card artifact1 = new Ornithopter();
        Card artifact2 = new IzzetCluestone();
        Card untouched = new Plains();
        harness.setLibrary(player1, List.of(artifact1, artifact2, untouched));

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(artifact1.getId(), artifact2.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(artifact1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact1).doesNotContain(artifact2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, artifact2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void summoningSickCawlCannotActivateEitherTapAbility() {
        Permanent cawl = harness.addToBattlefieldAndReturn(player1, new BelisariusCawl());
        cawl.setSummoningSick(true);
        addCreatureReady(player1, new Ornithopter());
        addCreatureReady(player1, new Ornithopter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cawl.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
