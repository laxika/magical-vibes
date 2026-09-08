package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Cloudpost;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Glimmerpost;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonumentToPerfectionTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability searches for a basic, Sphere, or Locus land")
    void searchesForBasicSphereOrLocusLand() {
        harness.addToBattlefield(player1, new MonumentToPerfection());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Card sphere = sphereLand("The Seedcore");
        Glimmerpost glimmerpost = new Glimmerpost();
        harness.setLibrary(player1, List.of(new Forest(), glimmerpost, sphere, new Cloudpost(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Glimmerpost", "The Seedcore", "Cloudpost");
        assertThat(search.params().reveals()).isTrue();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(search.params().cards().indexOf(glimmerpost)));

        harness.assertInHand(player1, "Glimmerpost");
    }

    @Test
    @DisplayName("The second ability needs nine differently named eligible lands")
    void animationRequiresNineDistinctEligibleLandNames() {
        harness.addToBattlefield(player1, new MonumentToPerfection());
        addEligibleLands(new Forest(), new Island(), new Mountain(), new Plains(), new Swamp(),
                new Cloudpost(), new Glimmerpost(), sphereLand("The Seedcore"), new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different names");
    }

    @Test
    @DisplayName("The second ability animates Monument to Perfection until end of turn")
    void animationMakesMonumentA9By9IndestructibleToxicCreature() {
        Permanent monument = harness.addToBattlefieldAndReturn(player1, new MonumentToPerfection());
        addEligibleLands(new Forest(), new Island(), new Mountain(), new Plains(), new Swamp(),
                new Cloudpost(), new Glimmerpost(), sphereLand("The Seedcore"), sphereLand("The Mycosynth Gardens"));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, monument)).isTrue();
        assertThat(gqs.isArtifact(monument)).isTrue();
        assertThat(gqs.getEffectivePower(gd, monument)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, monument)).isEqualTo(9);
        assertThat(gqs.effectiveCreatureSubtypes(gd, monument))
                .containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.CONSTRUCT);
        assertThat(gqs.hasKeyword(gd, monument, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, monument, Keyword.TOXIC)).isTrue();
        assertThat(gs.getEffectiveActivatedAbilities(gd, monument)).isEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, monument)).isFalse();
        assertThat(gqs.hasKeyword(gd, monument, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, monument, Keyword.TOXIC)).isFalse();
        assertThat(gs.getEffectiveActivatedAbilities(gd, monument)).hasSize(2);
    }

    private void addEligibleLands(Card... lands) {
        for (Card land : lands) {
            harness.addToBattlefield(player1, land);
        }
    }

    private Card sphereLand(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.LAND);
        card.setSubtypes(List.of(CardSubtype.SPHERE));
        return card;
    }
}
