package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvatarOfMight.class, GrizzlyBears.class})
class AvatarOfMightTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot cast Avatar of Might for {G}{G} without cost reduction")
    void cannotCastWithoutReduction() {
        assertThatThrownBy(() -> harness.castFromHand(player1, new AvatarOfMight(), "{G}{G}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can cast Avatar of Might for {G}{G} when opponent controls four more creatures")
    void canCastWithReductionAtFourMoreCreatures() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }

        AvatarOfMight avatar = new AvatarOfMight();
        harness.castFromHand(player1, avatar, "{G}{G}");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isSameAs(avatar);
    }

    @Test
    @DisplayName("Cost reduction does not apply when opponent controls only three more creatures")
    void reductionDoesNotApplyWithOnlyThreeMoreCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }

        assertThatThrownBy(() -> harness.castFromHand(player1, new AvatarOfMight(), "{G}{G}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cost reduction does not reduce Avatar of Might's colored mana cost")
    void reductionDoesNotApplyToColoredMana() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }

        assertThatThrownBy(() -> harness.castFromHand(player1, new AvatarOfMight(), "{G}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessCombatDamage() {
        Permanent avatar = addCreatureReady(player1, new AvatarOfMight());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 6));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(avatar);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Avatar of Might resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        AvatarOfMight avatar = new AvatarOfMight();
        harness.castFromHand(player1, avatar, "{6}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == avatar);
    }
}
