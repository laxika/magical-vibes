package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Plains;
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

class CennsEnlistmentTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Cenn's Enlistment creates two 1/1 white Kithkin Soldier tokens")
    void createsTwoKithkinSoldierTokens() {
        harness.setHand(player1, List.of(new CennsEnlistment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> soldiers = kithkinSoldiers();
        assertThat(soldiers).hasSize(2);
        for (Permanent soldier : soldiers) {
            assertThat(soldier.getCard().getPower()).isEqualTo(1);
            assertThat(soldier.getCard().getToughness()).isEqualTo(1);
            assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(soldier.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(soldier.getCard().getSubtypes()).contains(CardSubtype.KITHKIN, CardSubtype.SOLDIER);
            assertThat(soldier.getCard().isToken()).isTrue();
        }
    }

    @Test
    @DisplayName("Retrace creates two Kithkin Soldier tokens and discards a land")
    void retraceCreatesTokensAndDiscardsLand() {
        harness.setGraveyard(player1, List.of(new CennsEnlistment()));
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(kithkinSoldiers()).hasSize(2);
        // The discarded Plains and the resolved Cenn's Enlistment both end up in the graveyard.
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Retrace returns Cenn's Enlistment to the graveyard, not exile, so it can be recast")
    void retraceReturnsToGraveyard() {
        harness.setGraveyard(player1, List.of(new CennsEnlistment()));
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cenn's Enlistment"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Cenn's Enlistment"));
    }

    @Test
    @DisplayName("Retrace puts Cenn's Enlistment on the stack as a sorcery without flashback disposition")
    void retracePutsOnStackAsSorcery() {
        harness.setGraveyard(player1, List.of(new CennsEnlistment()));
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castRetrace(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Cenn's Enlistment");
        assertThat(gd.stack.getFirst().isCastWithFlashback()).isFalse();
    }

    @Test
    @DisplayName("Retrace requires discarding a land card")
    void retraceRequiresLandDiscard() {
        harness.setGraveyard(player1, List.of(new CennsEnlistment()));
        harness.setHand(player1, List.of(new CennsEnlistment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Permanent> kithkinSoldiers() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kithkin Soldier"))
                .toList();
    }
}
