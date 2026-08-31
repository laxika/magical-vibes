package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({ExtractTheTruth.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class, Pacifism.class})
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
        Permanent creature = new Permanent(new GrizzlyBears());
        Permanent firstEnchantment = new Permanent(new GloriousAnthem());
        Permanent secondEnchantment = new Permanent(new GloriousAnthem());
        gd.playerBattlefields.get(player2.getId()).addAll(List.of(creature, firstEnchantment, secondEnchantment));
        harness.setHand(player1, List.of(new ExtractTheTruth()));
        addMana();

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

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

    private void castMode(int mode) {
        harness.setHand(player1, List.of(new ExtractTheTruth()));
        addMana();
        harness.castSorcery(player1, 0, mode, player2.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
