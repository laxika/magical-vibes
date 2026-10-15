package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BloodbraidElf;
import com.github.laxika.magicalvibes.cards.e.EternalScourge;
import com.github.laxika.magicalvibes.cards.m.MerfolkTrickster;
import com.github.laxika.magicalvibes.cards.p.PrecognitionField;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpawnsireOfUlamog;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrannithMagistrate.class, BloodbraidElf.class, EternalScourge.class,
        MerfolkTrickster.class, PrecognitionField.class, Shock.class, SpawnsireOfUlamog.class,
        ThinkTwice.class})
class DrannithMagistrateTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents can't cast spells from their graveyards")
    void opponentsCannotCastFromGraveyard() {
        harness.addToBattlefield(player1, new DrannithMagistrate());
        harness.setGraveyard(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        setupPlayer2Active();

        assertThatThrownBy(() -> harness.castFlashback(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyards");
    }

    @Test
    @DisplayName("The controller can cast a spell from their graveyard")
    void controllerCanCastFromGraveyard() {
        harness.addToBattlefield(player1, new DrannithMagistrate());
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setupPlayer1Active();

        harness.castFlashback(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Opponents can't cast spells from exile")
    void opponentsCannotCastFromExile() {
        harness.addToBattlefield(player1, new DrannithMagistrate());
        EternalScourge scourge = new EternalScourge();
        harness.setExile(player2, List.of(scourge));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        setupPlayer2Active();

        assertThatThrownBy(() -> harness.castFromExile(player2, scourge.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exile");
    }

    @Test
    @DisplayName("The controller can cast a spell from exile")
    void controllerCanCastFromExile() {
        harness.addToBattlefield(player1, new DrannithMagistrate());
        EternalScourge scourge = new EternalScourge();
        harness.setExile(player1, List.of(scourge));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        setupPlayer1Active();

        harness.castFromExile(player1, scourge.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Opponents can't cast spells from the top of their library")
    void opponentsCannotCastFromLibrary() {
        harness.addToBattlefield(player1, new DrannithMagistrate());
        harness.addToBattlefield(player2, new PrecognitionField());
        harness.setLibrary(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        setupPlayer2Active();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player2, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Spells can't be cast from libraries");
    }

    @Test
    @DisplayName("Opponents can still cast spells from their hands")
    void opponentsCanCastFromHand() {
        harness.addToBattlefield(player1, new DrannithMagistrate());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        setupPlayer2Active();

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The controller can cast a spell from the top of their library")
    void controllerCanCastFromLibrary() {
        harness.addToBattlefield(player1, new DrannithMagistrate());
        harness.addToBattlefield(player1, new PrecognitionField());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        setupPlayer1Active();

        harness.castAndResolveFromLibraryTop(player1, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The restriction ends when Magistrate leaves the battlefield")
    void opponentsCanCastFromExileAfterMagistrateDies() {
        harness.addToBattlefield(player1, new DrannithMagistrate());
        EternalScourge scourge = new EternalScourge();
        harness.setExile(player2, List.of(scourge));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        setupPlayer2Active();

        assertThatThrownBy(() -> harness.castFromExile(player2, scourge.getId()))
                .isInstanceOf(IllegalStateException.class);
        var magistrateId = harness.getPermanentId(player1, "Drannith Magistrate");
        harness.castAndResolveInstant(player2, 0, magistrateId);
        harness.castAndResolveInstant(player2, 0, magistrateId);
        harness.assertNotOnBattlefield(player1, "Drannith Magistrate");

        harness.castFromExile(player2, scourge.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Eternal Scourge");
    }

    @Test
    @DisplayName("Magistrate does not restrict casting after losing its abilities")
    void opponentsCanCastFromExileAfterMagistrateLosesAbilities() {
        harness.addToBattlefield(player1, new DrannithMagistrate());
        EternalScourge scourge = new EternalScourge();
        harness.setExile(player2, List.of(scourge));
        harness.setHand(player2, List.of(new MerfolkTrickster()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        setupPlayer2Active();

        harness.castCreature(player2, 0, 0,
                harness.getPermanentId(player1, "Drannith Magistrate"));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castFromExile(player2, scourge.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drannith Magistrate");
        harness.assertOnBattlefield(player2, "Eternal Scourge");
    }

    @Test
    @DisplayName("Opponents cannot cast a cascade hit from exile")
    void opponentsCannotCastCascadeHit() {
        harness.addToBattlefield(player1, new DrannithMagistrate());
        EternalScourge hit = new EternalScourge();
        harness.setLibrary(player2, List.of(hit));
        harness.setHand(player2, List.of(new BloodbraidElf()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        setupPlayer2Active();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.LibrarySearch) {
            harness.handleCardChosen(player2, 0);
        }

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(hit.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(hit);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Bloodbraid Elf");
        harness.assertNotOnBattlefield(player2, "Eternal Scourge");
    }

    @Test
    @DisplayName("Opponents cannot cast Eldrazi spells from outside the game")
    void opponentsCannotCastFromOutsideGame() {
        harness.addToBattlefield(player1, new DrannithMagistrate());
        harness.addToBattlefield(player2, new SpawnsireOfUlamog());
        EternalScourge scourge = new EternalScourge();
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(scourge)));
        harness.addMana(player2, ManaColor.COLORLESS, 20);
        setupPlayer2Active();

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player2, true);
        }

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(scourge.getId()));
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(scourge);
        harness.assertNotOnBattlefield(player2, "Eternal Scourge");
    }

    private void setupPlayer1Active() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
