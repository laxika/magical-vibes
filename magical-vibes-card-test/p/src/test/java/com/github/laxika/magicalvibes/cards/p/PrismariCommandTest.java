package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BuryInBooks;
import com.github.laxika.magicalvibes.cards.c.CogworkArchivist;
import com.github.laxika.magicalvibes.cards.l.LetterOfAcceptance;
import com.github.laxika.magicalvibes.cards.n.NoviceDissector;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrismariCommand.class, LetterOfAcceptance.class, NoviceDissector.class,
        CogworkArchivist.class, BuryInBooks.class})
class PrismariCommandTest extends BaseCardTest {

    @Test
    void dealsDamageAndDestroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LetterOfAcceptance());
        prepareSpell();

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 3},
                List.of(player2.getId(), artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Letter of Acceptance");
    }

    @Test
    void drawsDiscardsAndCreatesTreasureForTheSameTargetPlayer() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new LetterOfAcceptance(), new NoviceDissector()));
        prepareSpell();

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{1, 2},
                List.of(player2.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void destroyArtifactModeRejectsNonArtifactTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NoviceDissector());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 3},
                List.of(player2.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damagesCreatureAndCreatesTreasureForController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NoviceDissector());
        prepareSpell();

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 2},
                List.of(creature.getId(), player1.getId()));
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Novice Dissector");
        harness.assertOnBattlefield(player1, "Treasure");
        harness.assertNotOnBattlefield(player2, "Treasure");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void drawsAndDiscardsForOnePlayerThenCreatesTreasureForAnother() {
        harness.setHand(player2, List.of(new NoviceDissector()));
        harness.setLibrary(player2, List.of(new LetterOfAcceptance(), new LetterOfAcceptance()));
        prepareSpell();

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{1, 2},
                List.of(player2.getId(), player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        harness.assertNotOnBattlefield(player1, "Treasure");
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 1);

        harness.assertInHand(player2, "Novice Dissector");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Treasure");
        harness.assertNotOnBattlefield(player2, "Treasure");
    }

    @Test
    void damageStillResolvesWhenArtifactTargetIsSacrificedInResponse() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LetterOfAcceptance());
        harness.setLibrary(player2, List.of(new NoviceDissector()));
        prepareSpell();

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 3},
                List.of(player2.getId(), artifact.getId()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Letter of Acceptance");
        harness.assertInHand(player2, "Novice Dissector");
        harness.assertInGraveyard(player1, "Prismari Command");
    }

    @Test
    void canTargetControllerForBothDamageAndLooting() {
        prepareSpell();
        harness.setLibrary(player1, List.of(new LetterOfAcceptance(), new NoviceDissector()));

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 1},
                List.of(player1.getId(), player1.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Letter of Acceptance");
        harness.assertInGraveyard(player1, "Novice Dissector");
        harness.assertInGraveyard(player1, "Prismari Command");
    }

    @Test
    void artifactDestructionWaitsUntilLootingIsComplete() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LetterOfAcceptance());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new LetterOfAcceptance(), new NoviceDissector()));
        prepareSpell();

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{1, 3},
                List.of(player2.getId(), artifact.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Letter of Acceptance");
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        harness.assertNotOnBattlefield(player2, "Letter of Acceptance");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    void createsTreasureAndDestroysAnExistingArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LetterOfAcceptance());
        prepareSpell();

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{2, 3},
                List.of(player2.getId(), artifact.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Treasure");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Letter of Acceptance");
        harness.assertInGraveyard(player1, "Letter of Acceptance");
    }

    @Test
    void cannotChooseTheDamageModeTwice() {
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 0},
                List.of(player1.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDamageAndDestroyTheSameArtifactCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CogworkArchivist());
        prepareSpell();

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 3},
                List.of(creature.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cogwork Archivist");
        harness.assertInGraveyard(player2, "Cogwork Archivist");
        harness.assertInGraveyard(player1, "Prismari Command");
    }

    @Test
    void doesNothingWhenBothTargetsBecomeIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CogworkArchivist());
        harness.setLibrary(player2, List.of(new NoviceDissector()));
        prepareSpell();

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 3},
                List.of(creature.getId(), creature.getId()));
        harness.setHand(player2, List.of(new BuryInBooks()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.playerDecks.get(player2.getId()).get(1).getId()).isEqualTo(creature.getCard().getId());
        harness.assertNotInGraveyard(player2, "Cogwork Archivist");
        harness.assertInGraveyard(player1, "Prismari Command");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new PrismariCommand()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
