package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.b.BelligerentYearling;
import com.github.laxika.magicalvibes.cards.o.OrazcaPuzzleDoor;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PoeticIngenuity.class, GrizzlyBears.class, Spellbook.class,
        BelligerentYearling.class, Abrade.class, OrazcaPuzzleDoor.class})
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
        return addCreatureReady(player, new BelligerentYearling());
    }

    private Permanent addNonDinosaurReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    @Test
    void treasureCountRemembersDinosaursThatAttackedBeforeRemoval() {
        harness.addToBattlefield(player1, new PoeticIngenuity());
        Permanent dinosaur = addDinosaurReady(player1);
        addDinosaurReady(player1);
        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 2);

        declareAttackers(List.of(1, 2));
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player2, 0, 0, dinosaur.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Belligerent Yearling")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void opponentsDinosaursDoNotTriggerTreasureCreation() {
        harness.addToBattlefield(player1, new PoeticIngenuity());
        addDinosaurReady(player2);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void dinosaurIsCreatedBeforeArtifactResolves() {
        harness.addToBattlefield(player1, new PoeticIngenuity());
        harness.setHand(player1, List.of(new OrazcaPuzzleDoor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Dinosaur")).hasSize(1);
        assertThat(findPermanents(player1, "Orazca Puzzle-Door")).isEmpty();
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Orazca Puzzle-Door");
    }

    @Test
    void opponentsArtifactDoesNotConsumeControllersTrigger() {
        harness.addToBattlefield(player1, new PoeticIngenuity());
        harness.setHand(player2, List.of(new OrazcaPuzzleDoor()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castArtifact(player2, 0);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Dinosaur")).isEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OrazcaPuzzleDoor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dinosaur")).hasSize(1);
    }

    @Test
    void artifactTriggerIsAvailableAgainOnANewTurn() {
        harness.addToBattlefield(player1, new PoeticIngenuity());
        harness.setHand(player1, List.of(new OrazcaPuzzleDoor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        advanceToUpkeep(player2);
        advanceToUpkeep(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OrazcaPuzzleDoor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dinosaur")).hasSize(2);
    }

    @Test
    void nonArtifactSpellDoesNotConsumeArtifactTrigger() {
        harness.addToBattlefield(player1, new PoeticIngenuity());
        harness.setHand(player1, List.of(new GrizzlyBears(), new OrazcaPuzzleDoor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dinosaur")).hasSize(1);
    }

    @Test
    void eachCopyHasItsOwnOncePerTurnLimit() {
        harness.addToBattlefield(player1, new PoeticIngenuity());
        harness.addToBattlefield(player1, new PoeticIngenuity());
        harness.setHand(player1, List.of(new OrazcaPuzzleDoor(), new OrazcaPuzzleDoor()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dinosaur")).hasSize(2);
    }
}
