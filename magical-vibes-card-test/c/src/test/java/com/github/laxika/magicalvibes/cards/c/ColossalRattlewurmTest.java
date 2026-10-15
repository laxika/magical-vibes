package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArmoredArmadillo;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SunscorchedDesert;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ColossalRattlewurm.class, Forest.class, SunscorchedDesert.class, ArmoredArmadillo.class})
class ColossalRattlewurmTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast at instant speed while its controller controls a Desert")
    void canBeCastAtInstantSpeedWithDesert() {
        harness.addToBattlefield(player1, new SunscorchedDesert());
        prepareInstantSpeedCast();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot be cast at instant speed without a Desert")
    void cannotBeCastAtInstantSpeedWithoutDesert() {
        prepareInstantSpeedCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Its graveyard ability exiles it and puts a Desert onto the battlefield tapped")
    void graveyardAbilityFetchesTappedDesert() {
        Card wurm = new ColossalRattlewurm();
        Card desert = new SunscorchedDesert();
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(wurm));
        harness.setLibrary(player1, List.of(desert, forest));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(wurm.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(wurm.getId()));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .extracting(Card::getId)
                .containsExactly(desert.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(desert.getId())
                        && permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentsDesertDoesNotGrantFlash() {
        harness.addToBattlefield(player2, new SunscorchedDesert());
        prepareInstantSpeedCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void desertInGraveyardDoesNotGrantFlash() {
        harness.setGraveyard(player1, List.of(new SunscorchedDesert()));
        prepareInstantSpeedCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void canBeCastNormallyWithoutDesert() {
        harness.castFromHand(player1, new ColossalRattlewurm(), "{2}{G}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Colossal Rattlewurm");
    }

    @Test
    void graveyardAbilityCanBeActivatedOnOpponentsTurnWithoutDesert() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Card wurm = new ColossalRattlewurm();
        harness.setGraveyard(player1, List.of(wurm));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passPriority(player2);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wurm);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void graveyardAbilityResolvesWhenLibraryContainsNoDesert() {
        Card wurm = new ColossalRattlewurm();
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(wurm));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wurm);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canFailToFindEvenWhenDesertIsAvailable() {
        Card desert = new SunscorchedDesert();
        harness.setGraveyard(player1, List.of(new ColossalRattlewurm()));
        harness.setLibrary(player1, List.of(desert));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(desert);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void insufficientManaDoesNotExileSource() {
        Card wurm = new ColossalRattlewurm();
        harness.setGraveyard(player1, List.of(wurm));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(wurm);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tramplesOverBlockerWithoutDesert() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ColossalRattlewurm());
        Permanent blocker = addCreatureReady(player2, new ArmoredArmadillo());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 4, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Armored Armadillo");
        harness.assertOnBattlefield(player1, "Colossal Rattlewurm");
    }

    private void prepareInstantSpeedCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ColossalRattlewurm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passPriority(player2);
    }
}
