package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.d.DoomedDissenter;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GeralfsMindcrusher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrawlingInfestation.class, Forest.class, GeralfsMindcrusher.class, GrizzlyBears.class,
        Abrade.class, DoomedDissenter.class})
class CrawlingInfestationTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the upkeep trigger mills two cards")
    void upkeepMillsTwoCards() {
        harness.addToBattlefield(player1, new CrawlingInfestation());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining the upkeep trigger does not mill")
    void decliningUpkeepDoesNotMill() {
        harness.addToBattlefield(player1, new CrawlingInfestation());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Milling multiple creature cards creates only one Insect token per turn")
    void millingMultipleCreaturesCreatesOneInsectTokenPerTurn() {
        harness.addToBattlefield(player1, new CrawlingInfestation());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature cards put into your graveyard during an opponent's turn do not trigger")
    void opponentTurnGraveyardEventDoesNotTrigger() {
        harness.addToBattlefield(player1, new CrawlingInfestation());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Forest(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GeralfsMindcrusher()));
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.castCreature(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Insect")).isZero();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
    }

    @Test
    void millingOnlyNoncreaturesDoesNotCreateInsect() {
        harness.addToBattlefield(player1, new CrawlingInfestation());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(countPermanents(player1, "Insect")).isZero();
    }

    @Test
    void shortLibraryMillsAvailableCreatureAndCreatesInsect() {
        harness.addToBattlefield(player1, new CrawlingInfestation());
        harness.setLibrary(player1, List.of(new DoomedDissenter()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Doomed Dissenter");
        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
        assertThat(countPermanents(player1, "Zombie")).isZero();
    }

    @Test
    void emptyLibraryDoesNotCreateInsect() {
        harness.addToBattlefield(player1, new CrawlingInfestation());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Insect")).isZero();
    }

    @Test
    void creatureDeathTriggersButLaterDeathInSameTurnDoesNot() {
        harness.addToBattlefield(player1, new CrawlingInfestation());
        harness.addToBattlefield(player1, new DoomedDissenter());
        harness.addToBattlefield(player1, new DoomedDissenter());
        harness.setHand(player1, List.of(new Abrade(), new Abrade()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, 0, harness.getPermanentId(player1, "Doomed Dissenter"));
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);

        harness.castInstant(player1, 0, 0, harness.getPermanentId(player1, "Doomed Dissenter"));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(2);
    }

    @Test
    void opponentsCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new CrawlingInfestation());
        harness.addToBattlefield(player2, new DoomedDissenter());
        harness.setHand(player1, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "Doomed Dissenter"));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Doomed Dissenter");
        assertThat(countPermanents(player1, "Insect")).isZero();
    }

    @Test
    void twoCopiesEachCreateAnInsect() {
        harness.addToBattlefield(player1, new CrawlingInfestation());
        harness.addToBattlefield(player1, new CrawlingInfestation());
        harness.addToBattlefield(player1, new DoomedDissenter());
        harness.setHand(player1, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, 0, harness.getPermanentId(player1, "Doomed Dissenter"));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Insect")).isEqualTo(2);
    }

    @Test
    void killingAnInsectTokenDoesNotTrigger() {
        harness.addToBattlefield(player1, new CrawlingInfestation());
        harness.setLibrary(player1, List.of(new DoomedDissenter(), new Forest()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.addToBattlefield(player1, new CrawlingInfestation());
        harness.setHand(player1, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, 0, harness.getPermanentId(player1, "Insect"));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Insect")).isZero();
    }
}
