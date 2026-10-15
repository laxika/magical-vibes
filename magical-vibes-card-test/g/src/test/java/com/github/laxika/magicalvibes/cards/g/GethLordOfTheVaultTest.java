package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.d.DarksteelColossus;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.m.MoriokReaver;
import com.github.laxika.magicalvibes.cards.s.SylvokLifestaff;
import com.github.laxika.magicalvibes.cards.u.UndeadAlchemist;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GethLordOfTheVault.class, MoriokReaver.class, CarapaceForger.class,
        Memnite.class, SylvokLifestaff.class, GraspOfDarkness.class})
class GethLordOfTheVaultTest extends BaseCardTest {

    @Test
    @DisplayName("Intimidate — same color creature can block")
    void sameColorCanBlock() {
        Permanent gethPerm = addCreatureReady(player1, new GethLordOfTheVault());
        gethPerm.setAttacking(true);

        // MoriokReaver is black — same color as Geth
        addCreatureReady(player2, new MoriokReaver());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Intimidate — artifact creature can block")
    void artifactCreatureCanBlock() {
        Permanent gethPerm = addCreatureReady(player1, new GethLordOfTheVault());
        gethPerm.setAttacking(true);

        addCreatureReady(player2, new Memnite());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Intimidate — different color non-artifact creature cannot block")
    void differentColorCannotBlock() {
        Permanent gethPerm = addCreatureReady(player1, new GethLordOfTheVault());
        gethPerm.setAttacking(true);

        // CarapaceForger is green — different color from Geth (black)
        addCreatureReady(player2, new CarapaceForger());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block")
                .hasMessageContaining("(intimidate)");
    }

    @Test
    @DisplayName("Activated ability — takes creature from opponent's graveyard onto battlefield tapped")
    void takesCreatureFromOpponentGraveyard() {
        harness.addToBattlefield(player1, new GethLordOfTheVault());

        // CarapaceForger has mana value 2 — put in opponent's graveyard
        Card bears = new CarapaceForger();
        harness.setGraveyard(player2, List.of(bears));

        // Add some cards to opponent's deck for milling
        Card deckCard1 = new MoriokReaver();
        Card deckCard2 = new MoriokReaver();
        harness.setLibrary(player2, List.of(deckCard1, deckCard2));

        // X=2 (mana value of CarapaceForger), plus {B}
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Bears should be on player1's battlefield, tapped
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(bears.getId()) && p.isTapped());

        // Bears removed from opponent's graveyard (check by card ID)
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(bears.getId()));

        // Opponent should have been milled 2 cards (Moriok Reavers)
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(deckCard1, deckCard2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Activated ability — takes artifact from opponent's graveyard")
    void takesArtifactFromOpponentGraveyard() {
        harness.addToBattlefield(player1, new GethLordOfTheVault());

        Card artifact = new SylvokLifestaff();
        harness.setGraveyard(player2, List.of(artifact));
        Card milled = new MoriokReaver();
        harness.setLibrary(player2, List.of(milled));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, artifact.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Artifact should be on player1's battlefield, tapped
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(artifact.getId()) && p.isTapped());
    }

    @Test
    @DisplayName("Activated ability — rejects target with wrong mana value")
    void rejectsWrongManaValue() {
        harness.addToBattlefield(player1, new GethLordOfTheVault());

        // CarapaceForger has mana value 2, but we'll use X=3
        Card bears = new CarapaceForger();
        harness.setGraveyard(player2, List.of(bears));

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value must equal X");
    }

    @Test
    @DisplayName("Activated ability — rejects targeting own graveyard")
    void rejectsOwnGraveyard() {
        harness.addToBattlefield(player1, new GethLordOfTheVault());

        // Put creature in player1's own graveyard
        Card bears = new CarapaceForger();
        harness.setGraveyard(player1, List.of(bears));

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("allowed graveyard");
    }

    @Test
    @DisplayName("Stolen creature goes to original owner's graveyard on death")
    void stolenCreatureGoesToOriginalOwnerGraveyard() {
        harness.addToBattlefield(player1, new GethLordOfTheVault());

        Card bears = new CarapaceForger();
        harness.setGraveyard(player2, List.of(bears));

        // Add some deck cards for milling
        harness.setLibrary(player2, List.of(new CarapaceForger(), new CarapaceForger()));

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent stolenBears = findPermanent(player1, "Carapace Forger");
        stolenBears.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bears);
        harness.assertNotOnBattlefield(player1, "Carapace Forger");
    }

    @Test
    void zeroXReturnsArtifactCreatureWithoutMilling() {
        harness.addToBattlefield(player1, new GethLordOfTheVault());
        Card target = new Memnite();
        Card libraryCard = new MoriokReaver();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(libraryCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Memnite").isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
    }

    @Test
    void rejectsInstantEvenWhenManaValueMatchesX() {
        harness.addToBattlefield(player1, new GethLordOfTheVault());
        Card target = new GraspOfDarkness();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void missingTargetPreventsReanimationAndMilling() {
        harness.addToBattlefield(player1, new GethLordOfTheVault());
        Card target = new CarapaceForger();
        Card libraryCard = new MoriokReaver();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(libraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 2, target.getId(), Zone.GRAVEYARD);

        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Carapace Forger");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void reanimatesWithEmptyOpponentLibraryWithoutCausingLoss() {
        harness.addToBattlefield(player1, new GethLordOfTheVault());
        Card target = new CarapaceForger();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Carapace Forger").isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(com.github.laxika.magicalvibes.model.GameStatus.FINISHED);
    }

    @Test
    @CardUsed({DarksteelColossus.class})
    void millingAppliesShuffleIntoLibraryReplacement() {
        harness.addToBattlefield(player1, new GethLordOfTheVault());
        Card target = new SylvokLifestaff();
        Card colossus = new DarksteelColossus();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(colossus));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Sylvok Lifestaff").isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(colossus);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(colossus);
    }

    @Test
    @CardUsed({UndeadAlchemist.class})
    void millingTriggersAbilitiesForCreaturesPutIntoOpponentGraveyard() {
        harness.addToBattlefield(player1, new GethLordOfTheVault());
        harness.addToBattlefield(player1, new UndeadAlchemist());
        Card target = new SylvokLifestaff();
        Card milled = new MoriokReaver();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(milled));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(milled);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(milled);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().isToken());
    }
}
