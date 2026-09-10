package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UneasyPartings.class, GrizzlyBears.class, Island.class})
class UneasyPartingsTest extends BaseCardTest {

    @Test
    @DisplayName("The target's owner puts the creature on top of their library")
    void ownerPutsTargetOnTop() {
        Permanent target = addTarget(false);
        Card oldTop = new Island();
        setLibrary(player2, oldTop);
        cast(target, 4);

        harness.passBothPriorities();
        assertOwnerChoice(player2);
        harness.handleListChoice(player2, "Put it on top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), oldTop);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The target's owner puts the creature on the bottom of their library")
    void ownerPutsTargetOnBottom() {
        Permanent target = addTarget(false);
        Card oldTop = new Island();
        setLibrary(player2, oldTop);
        cast(target, 4);

        harness.passBothPriorities();
        harness.handleListChoice(player2, "Put it on the bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(oldTop, target.getCard());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Costs {2}{U} when targeting an attacking nontoken creature")
    void reducedCostForAttackingNontokenCreature() {
        Permanent target = addTarget(true);
        harness.setHand(player1, List.of(new UneasyPartings()));
        addMana(player1, 2);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Requires the full cost for a nonattacking creature")
    void fullCostForNonattackingCreature() {
        Permanent target = addTarget(false);
        harness.setHand(player1, List.of(new UneasyPartings()));
        addMana(player1, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires the full cost for an attacking token creature")
    void fullCostForAttackingTokenCreature() {
        GrizzlyBears tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, tokenCard);
        target.setAttacking(true);
        target.setAttackTarget(player1.getId());
        harness.setHand(player1, List.of(new UneasyPartings()));
        addMana(player1, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTarget(boolean attacking) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setAttacking(attacking);
        if (attacking) {
            target.setAttackTarget(player1.getId());
        }
        return target;
    }

    private void cast(Permanent target, int colorlessMana) {
        harness.setHand(player1, List.of(new UneasyPartings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.castInstant(player1, 0, target.getId());
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player, int colorlessMana) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, colorlessMana);
    }

    private void assertOwnerChoice(com.github.laxika.magicalvibes.model.Player owner) {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(owner.getId());
        assertThat(choice.options()).containsExactly("Put it on top", "Put it on the bottom");
    }

    private void setLibrary(com.github.laxika.magicalvibes.model.Player player, Card... cards) {
        gd.playerDecks.put(player.getId(), new ArrayList<>(List.of(cards)));
    }
}
