package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EssenceWarden;
import com.github.laxika.magicalvibes.cards.f.FontOfFertility;
import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HourOfNeed.class, GoldenHind.class, FontOfFertility.class, Hubris.class, EssenceWarden.class})
class HourOfNeedTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles each target creature and gives its controller a Sphinx")
    void exilesTargetsAndCreatesSphinxesForTheirControllers() {
        Permanent ownCreature = addCreatureReady(player1, new GoldenHind());
        Permanent opponentCreature = addCreatureReady(player2, new GoldenHind());

        harness.setHand(player1, List.of(new HourOfNeed()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, List.of(ownCreature.getId(), opponentCreature.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Golden Hind");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Golden Hind");

        assertThat(findPermanents(player1, "Sphinx")).hasSize(1);
        assertThat(findPermanents(player2, "Sphinx")).hasSize(1);
        for (Permanent sphinx : List.of(
                findPermanents(player1, "Sphinx").getFirst(),
                findPermanents(player2, "Sphinx").getFirst())) {
            assertThat(sphinx.getCard().getColor()).isEqualTo(CardColor.BLUE);
            assertThat(sphinx.getCard().getSubtypes()).containsExactly(CardSubtype.SPHINX);
            assertThat(gqs.getEffectivePower(gd, sphinx)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, sphinx)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, sphinx, Keyword.FLYING)).isTrue();
        }
    }

    @Test
    @DisplayName("Strive requires {1}{U} for each additional target")
    void striveAddsCostForEachAdditionalTarget() {
        Permanent firstCreature = addCreatureReady(player1, new GoldenHind());
        Permanent secondCreature = addCreatureReady(player1, new GoldenHind());

        harness.setHand(player1, List.of(new HourOfNeed()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May target no creatures")
    void noTargetsExilesNothing() {
        addCreatureReady(player2, new GoldenHind());
        harness.setHand(player1, List.of(new HourOfNeed()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, List.of());

        harness.assertOnBattlefield(player2, "Golden Hind");
        assertThat(findPermanents(player1, "Sphinx")).isEmpty();
        assertThat(findPermanents(player2, "Sphinx")).isEmpty();
    }

    @Test
    @DisplayName("Can target only creatures")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new FontOfFertility());
        harness.setHand(player1, List.of(new HourOfNeed()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID fontId = harness.getPermanentId(player1, "Font of Fertility");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fontId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A single target costs only the base mana cost")
    void singleTargetNeedsNoStrivePayment() {
        Permanent creature = addCreatureReady(player2, new GoldenHind());
        harness.setHand(player1, List.of(new HourOfNeed()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Golden Hind");
        assertThat(findPermanents(player2, "Sphinx")).hasSize(1);
    }

    @Test
    @DisplayName("Only a target still legal at resolution produces a Sphinx")
    void removedTargetProducesNoToken() {
        Permanent first = addCreatureReady(player2, new GoldenHind());
        Permanent second = addCreatureReady(player2, new GoldenHind());
        harness.setHand(player1, List.of(new HourOfNeed(), new Hubris()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.castAndResolveInstant(player1, 0, first.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Golden Hind");
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
        assertThat(findPermanents(player2, "Sphinx")).hasSize(1);
    }

    @Test
    @DisplayName("All illegal targets cause the spell to create no tokens")
    void allTargetsRemovedProducesNoTokens() {
        Permanent creature = addCreatureReady(player2, new GoldenHind());
        harness.setHand(player1, List.of(new HourOfNeed(), new Hubris()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hour of Need");
        assertThat(findPermanents(player2, "Sphinx")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiled creature tokens also produce replacement Sphinxes")
    void exilingTokenCreatesAnotherSphinx() {
        Permanent creature = addCreatureReady(player2, new GoldenHind());
        harness.setHand(player1, List.of(new HourOfNeed(), new HourOfNeed()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        UUID originalTokenId = findPermanent(player2, "Sphinx").getId();
        harness.castAndResolveInstant(player1, 0, originalTokenId);

        assertThat(findPermanents(player2, "Sphinx")).hasSize(1);
        assertThat(findPermanent(player2, "Sphinx").getId()).isNotEqualTo(originalTokenId);
    }

    @Test
    @DisplayName("Exile every target before creating any Sphinx tokens")
    void exiledWardenDoesNotSeeReplacementTokensEnter() {
        Permanent creature = addCreatureReady(player1, new GoldenHind());
        Permanent warden = addCreatureReady(player2, new EssenceWarden());
        harness.setHand(player1, List.of(new HourOfNeed()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId(), warden.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sphinx")).hasSize(1);
        assertThat(findPermanents(player2, "Sphinx")).hasSize(1);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Any number of targets allows more than ninety-nine creatures")
    void canExileOneHundredCreatures() {
        List<UUID> targets = java.util.stream.IntStream.range(0, 100)
                .mapToObj(i -> addCreatureReady(player2, new GoldenHind()).getId())
                .toList();
        harness.setHand(player1, List.of(new HourOfNeed()));
        harness.addMana(player1, ManaColor.BLUE, 100);
        harness.addMana(player1, ManaColor.COLORLESS, 101);

        harness.castAndResolveInstant(player1, 0, targets);

        assertThat(findPermanents(player2, "Golden Hind")).isEmpty();
        assertThat(findPermanents(player2, "Sphinx")).hasSize(100);
    }
}
