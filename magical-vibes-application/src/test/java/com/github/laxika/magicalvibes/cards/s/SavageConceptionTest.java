package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SavageConceptionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Savage Conception creates a 3/3 green Beast token")
    void createsBeastToken() {
        harness.setHand(player1, List.of(new SavageConception()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> beasts = beasts();
        assertThat(beasts).hasSize(1);
        Permanent beast = beasts.getFirst();
        assertThat(beast.getCard().getPower()).isEqualTo(3);
        assertThat(beast.getCard().getToughness()).isEqualTo(3);
        assertThat(beast.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(beast.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(beast.getCard().getSubtypes()).contains(CardSubtype.BEAST);
        assertThat(beast.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Retrace creates a Beast token and discards a land")
    void retraceCreatesTokenAndDiscardsLand() {
        harness.setGraveyard(player1, List.of(new SavageConception()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(beasts()).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Retrace returns Savage Conception to the graveyard, not exile, so it can be recast")
    void retraceReturnsToGraveyard() {
        harness.setGraveyard(player1, List.of(new SavageConception()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Savage Conception"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Savage Conception"));
    }

    @Test
    @DisplayName("Retrace puts Savage Conception on the stack as a sorcery without flashback disposition")
    void retracePutsOnStackAsSorcery() {
        harness.setGraveyard(player1, List.of(new SavageConception()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castRetrace(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Savage Conception");
        assertThat(gd.stack.getFirst().isCastWithFlashback()).isFalse();
    }

    @Test
    @DisplayName("Retrace requires discarding a land card")
    void retraceRequiresLandDiscard() {
        harness.setGraveyard(player1, List.of(new SavageConception()));
        harness.setHand(player1, List.of(new SavageConception()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Permanent> beasts() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Beast"))
                .toList();
    }
}
