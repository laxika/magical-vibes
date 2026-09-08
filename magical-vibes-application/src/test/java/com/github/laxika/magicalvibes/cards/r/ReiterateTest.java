package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reiterate.class, CounselOfTheSoratami.class, GrizzlyBears.class})
class ReiterateTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a target instant or sorcery spell")
    void copiesTargetInstantOrSorcerySpell() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        Reiterate reiterate = new Reiterate();
        harness.setHand(player1, List.of(counsel, reiterate));
        addReiterateMana(false);

        harness.castSorcery(player1, 0, (UUID) null);
        harness.castInstant(player1, 0, counsel.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(reiterate.getId()));
    }

    @Test
    @DisplayName("Buyback returns Reiterate to its owner's hand when it resolves")
    void buybackReturnsToHand() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        Reiterate reiterate = new Reiterate();
        harness.setHand(player1, List.of(counsel, reiterate));
        addReiterateMana(true);

        harness.castSorcery(player1, 0, (UUID) null);
        harness.castInstantWithBuyback(player1, 0, counsel.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(reiterate.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(reiterate.getId()));
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears, new Reiterate()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReiterateMana(boolean buyback) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, buyback ? 6 : 3);
    }
}
