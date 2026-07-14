package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MindwrackLiegeTest extends BaseCardTest {

    // ===== Static effects: +1/+1 to own blue / red creatures =====

    @Test
    @DisplayName("Other blue creatures you control get +1/+1")
    void buffsOwnBlueCreatures() {
        harness.addToBattlefield(player1, new MindwrackLiege());
        harness.addToBattlefield(player1, new FugitiveWizard());

        Permanent wizard = findPermanent(player1, "Fugitive Wizard");
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Other red creatures you control get +1/+1")
    void buffsOwnRedCreatures() {
        harness.addToBattlefield(player1, new MindwrackLiege());
        harness.addToBattlefield(player1, new HillGiant());

        Permanent giant = findPermanent(player1, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not buff non-blue non-red creatures")
    void doesNotBuffOffColorCreatures() {
        harness.addToBattlefield(player1, new MindwrackLiege());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Activated ability: put a blue or red creature card onto the battlefield =====

    @Test
    @DisplayName("Only blue or red creature cards in hand are valid choices")
    void onlyBlueOrRedCreaturesAreValidChoices() {
        harness.addToBattlefield(player1, new MindwrackLiege());
        // Blue (0), off-color green (1), red (2).
        harness.setHand(player1, List.of(new FugitiveWizard(), new GrizzlyBears(), new HillGiant()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0, 2);
    }

    @Test
    @DisplayName("Choosing a red creature puts it onto the battlefield")
    void choosingRedCreaturePutsItOntoBattlefield() {
        harness.addToBattlefield(player1, new MindwrackLiege());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hill Giant"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the may leaves hand and battlefield unchanged")
    void decliningMayLeavesHandUnchanged() {
        harness.addToBattlefield(player1, new MindwrackLiege());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        int battlefieldSizeBefore = harness.getGameData().playerBattlefields.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldSizeBefore);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new MindwrackLiege());
        harness.addMana(player1, ManaColor.BLUE, 3);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
