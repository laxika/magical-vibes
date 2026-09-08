package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PoeticIngenuity.class, GrizzlyBears.class, Spellbook.class})
class PoeticIngenuityTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Treasure for each Dinosaur that attacks")
    void createsTreasureForEachAttackingDinosaur() {
        addCreatureReady(player1, new PoeticIngenuity());
        addDinosaurReady(player1);
        addDinosaurReady(player1);
        addNonDinosaurReady(player1);

        declareAttackers(List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger when no Dinosaur attacks")
    void doesNotTriggerWithoutAttackingDinosaur() {
        addCreatureReady(player1, new PoeticIngenuity());
        addNonDinosaurReady(player1);

        declareAttackers(List.of(1));

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creates a 3/1 red Dinosaur token for the first artifact spell each turn")
    void createsDinosaurTokenForFirstArtifactSpellEachTurn() {
        harness.addToBattlefield(player1, new PoeticIngenuity());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dinosaur")).singleElement().satisfies(dinosaur -> {
            assertThat(dinosaur.getCard().isToken()).isTrue();
            assertThat(dinosaur.getCard().getPower()).isEqualTo(3);
            assertThat(dinosaur.getCard().getToughness()).isEqualTo(1);
            assertThat(dinosaur.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(dinosaur.getCard().getSubtypes()).contains(CardSubtype.DINOSAUR);
        });
    }

    @Test
    @DisplayName("Does not trigger for a non-artifact spell")
    void doesNotTriggerForNonArtifactSpell() {
        harness.addToBattlefield(player1, new PoeticIngenuity());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dinosaur")).isEmpty();
    }

    private Permanent addDinosaurReady(Player player) {
        Card dinosaur = new Card();
        dinosaur.setName("Test Dinosaur");
        dinosaur.setType(CardType.CREATURE);
        dinosaur.setColor(CardColor.RED);
        dinosaur.setSubtypes(List.of(CardSubtype.DINOSAUR));
        dinosaur.setPower(2);
        dinosaur.setToughness(2);
        return addCreatureReady(player, dinosaur);
    }

    private Permanent addNonDinosaurReady(Player player) {
        Card creature = new Card();
        creature.setName("Test Creature");
        creature.setType(CardType.CREATURE);
        creature.setColor(CardColor.GREEN);
        creature.setSubtypes(List.of(CardSubtype.BEAST));
        creature.setPower(2);
        creature.setToughness(2);
        return addCreatureReady(player, creature);
    }
}
