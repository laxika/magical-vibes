package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuicksilverAmuletTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability taps the Amulet, spends {4}, and goes on the stack")
    void activatingAbilityUsesTapAndMana() {
        Permanent amulet = addAmulet();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(amulet.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Quicksilver Amulet");
    }

    @Test
    @DisplayName("Accepting the may choice offers only creature cards in hand")
    void resolvingPromptsOnlyCreatureChoices() {
        addAmulet();
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Choosing a creature puts it onto the battlefield untapped")
    void choosingCreaturePutsItOntoBattlefield() {
        addAmulet();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Grizzly Bears".equals(permanent.getCard().getName()))
                .allMatch(permanent -> !permanent.isTapped())).isTrue();
    }

    @Test
    @DisplayName("Declining the may choice leaves hand and battlefield unchanged")
    void decliningMayLeavesHandUnchanged() {
        addAmulet();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();
        int battlefieldSizeBefore = harness.getGameData().playerBattlefields.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldSizeBefore);
    }

    @Test
    @DisplayName("Ability does not prompt when controller has no creature cards in hand")
    void noCreaturesInHandSkipsChoice() {
        addAmulet();
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("has no creature cards in hand"));
    }

    @Test
    @DisplayName("Cannot activate ability while tapped")
    void cannotActivateWhileTapped() {
        Permanent amulet = addAmulet();
        amulet.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addAmulet();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addAmulet() {
        Permanent amulet = new Permanent(new QuicksilverAmulet());
        amulet.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(amulet);
        return amulet;
    }
}
