package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({MoldervineCloak.class, Forest.class, GrizzlyBears.class})
class MoldervineCloakTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+3 while Moldervine Cloak is attached")
    void boostsEnchantedCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent cloak = harness.addToBattlefieldAndReturn(player1, new MoldervineCloak());
        cloak.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(cloak);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Moldervine Cloak can dredge two cards into its owner's hand")
    void dredgesInsteadOfDrawing() {
        MoldervineCloak cloak = new MoldervineCloak();
        List<Card> milled = List.of(new Forest(), new GrizzlyBears());
        harness.setGraveyard(player1, List.of(cloak));
        harness.setLibrary(player1, milled);

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(cloak);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Declining Moldervine Cloak's dredge draws normally")
    void declinesDredge() {
        MoldervineCloak cloak = new MoldervineCloak();
        Card topCard = new Forest();
        harness.setGraveyard(player1, List.of(cloak));
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears()));

        resolveDraw();
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cloak);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Moldervine Cloak can target only a creature")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new MoldervineCloak()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void resolveDraw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
