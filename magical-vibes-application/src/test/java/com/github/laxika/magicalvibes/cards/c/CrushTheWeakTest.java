package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrushTheWeakTest extends BaseCardTest {

    private void castCrushTheWeak() {
        harness.setHand(player1, List.of(new CrushTheWeak()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals 2 damage to each creature and exiles creatures killed by that damage")
    void exilesCreaturesKilledByDamage() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new FugitiveWizard());

        castCrushTheWeak();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertNotInGraveyard(player1, "Fugitive Wizard");
        harness.assertNotInGraveyard(player2, "Fugitive Wizard");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fugitive Wizard"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Fugitive Wizard"));
    }

    @Test
    @DisplayName("Does not exile a creature that survives the damage")
    void survivingCreaturesRemain() {
        harness.addToBattlefield(player2, new SerraAngel());

        castCrushTheWeak();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Serra Angel"));
    }

    @Test
    @DisplayName("Does not deal damage to a creature with protection from red")
    void protectedCreatureIsUntouched() {
        harness.addToBattlefield(player2, new PaladinEnVec());

        castCrushTheWeak();

        harness.assertOnBattlefield(player2, "Paladin en-Vec");
    }

    @Test
    @DisplayName("Foretell exiles the card face down and permits casting it on a later turn")
    void foretellsAndCastsOnLaterTurn() {
        CrushTheWeak spell = new CrushTheWeak();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(spell.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Crush the Weak");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
