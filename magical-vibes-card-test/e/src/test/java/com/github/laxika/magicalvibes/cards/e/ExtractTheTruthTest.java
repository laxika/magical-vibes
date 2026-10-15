package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.ObNixilisTheAdversary;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExtractTheTruth.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class,
        ObNixilisTheAdversary.class, Pacifism.class})
class ExtractTheTruthTest extends BaseCardTest {

    @Test
    @DisplayName("Mode one lets the controller discard an eligible card from the revealed hand")
    void discardsEligibleCardFromHand() {
        Card creature = new GrizzlyBears();
        Card enchantment = new Pacifism();
        Card land = new Forest();
        harness.setHand(player2, List.of(creature, enchantment, land));
        castMode(0);

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0, 1);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .contains(enchantment.getId());
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(creature.getId(), land.getId());
    }

    @Test
    @DisplayName("Mode one does not force a discard when no eligible card is revealed")
    void doesNotDiscardWhenNoEligibleCardExists() {
        Card land = new Forest();
        harness.setHand(player2, List.of(land));
        castMode(0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(land.getId());
    }

    @Test
    @DisplayName("Mode two makes the targeted opponent choose an enchantment to sacrifice")
    void sacrificesChosenEnchantment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent firstEnchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent secondEnchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        castMode(1);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstEnchantment.getId(), secondEnchantment.getId());
        assertThat(choice.validIds()).doesNotContain(creature.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(secondEnchantment.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(creature.getId(), firstEnchantment.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .contains(secondEnchantment.getCard().getId());
    }

    @Test
    @DisplayName("Neither mode can target the spell's controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new ExtractTheTruth()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode one allows declining to discard even when an eligible card is revealed")
    void mayDeclineDiscard() {
        Card creature = new GrizzlyBears();
        harness.setHand(player2, List.of(creature));
        castMode(0);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId).containsExactly(creature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mode one reveals the whole hand and discards a chosen creature")
    void revealsHandAndDiscardsCreature() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setHand(player2, List.of(creature, land));
        castMode(0);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("reveals their hand")
                        && log.contains("Grizzly Bears") && log.contains("Forest"));
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId).containsExactly(creature.getId());
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId).containsExactly(land.getId());
    }

    @Test
    @DisplayName("Mode one allows choosing a planeswalker and rejects a land")
    void discardsPlaneswalkerButNotLand() {
        Card planeswalker = new ObNixilisTheAdversary();
        Card land = new Forest();
        harness.setHand(player2, List.of(planeswalker, land));
        castMode(0);

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId).containsExactly(planeswalker.getId());
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId).containsExactly(land.getId());
    }

    @Test
    @DisplayName("Mode one resolves without a choice when the opponent's hand is empty")
    void resolvesAgainstEmptyHand() {
        harness.setHand(player2, List.of());
        castMode(0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Extract the Truth");
    }

    @Test
    @DisplayName("Mode two resolves without sacrificing a nonenchantment or the controller's enchantment")
    void resolvesWhenOpponentHasNoEnchantment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        castMode(1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(enchantment);
        harness.assertInGraveyard(player1, "Extract the Truth");
    }

    @Test
    @DisplayName("Mode two sacrifices the only enchantment without requiring a choice")
    void sacrificesOnlyEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        castMode(1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Mode two cannot target the spell's controller")
    void sacrificeModeCannotTargetController() {
        harness.setHand(player1, List.of(new ExtractTheTruth()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMode(int mode) {
        harness.setHand(player1, List.of(new ExtractTheTruth()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, mode, player2.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
