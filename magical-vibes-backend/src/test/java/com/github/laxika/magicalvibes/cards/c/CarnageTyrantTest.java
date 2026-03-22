package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpiketailHatchling;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarnageTyrantTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Carnage Tyrant has cant-be-countered static effect")
    void hasCantBeCounteredEffect() {
        CarnageTyrant card = new CarnageTyrant();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasAtLeastOneElementOfType(CantBeCounteredEffect.class);
    }

    // ===== Can't be countered =====

    @Test
    @DisplayName("Carnage Tyrant cannot be countered by Cancel")
    void cannotBeCounteredByCancel() {
        CarnageTyrant tyrant = new CarnageTyrant();
        harness.setHand(player1, List.of(tyrant));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.setHand(player2, List.of(new com.github.laxika.magicalvibes.cards.c.Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, tyrant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Carnage Tyrant"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Carnage Tyrant"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cancel"));
    }

    @Test
    @DisplayName("Carnage Tyrant cannot be countered by counter-unless-pays abilities")
    void cannotBeCounteredByCounterUnlessPays() {
        harness.addToBattlefield(player2, new SpiketailHatchling());

        CarnageTyrant tyrant = new CarnageTyrant();
        harness.setHand(player1, List.of(tyrant));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, tyrant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Carnage Tyrant"));
    }

    // ===== Hexproof =====

    @Test
    @DisplayName("Opponent cannot target Carnage Tyrant with spells")
    void opponentCannotTargetWithSpells() {
        Permanent tyrantPerm = addTyrantReady(player1);

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, tyrantPerm.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Controller can target own Carnage Tyrant with spells")
    void controllerCanTargetOwnTyrant() {
        Permanent tyrantPerm = addTyrantReady(player1);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, tyrantPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Giant Growth");
    }

    @Test
    @DisplayName("Carnage Tyrant has hexproof keyword on the battlefield")
    void hasHexproofKeyword() {
        Permanent tyrantPerm = addTyrantReady(player1);

        assertThat(gqs.hasKeyword(gd, tyrantPerm, Keyword.HEXPROOF)).isTrue();
    }

    // ===== Trample =====

    @Test
    @DisplayName("Carnage Tyrant deals excess combat damage to defending player via trample")
    void trampleDealExcessDamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent tyrantPerm = addTyrantReady(player1);
        tyrantPerm.setAttacking(true);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Carnage Tyrant is 7/6, blocker is 2/2 → assign lethal (2) to blocker, excess (5) to player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 5
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Helper methods =====

    private Permanent addTyrantReady(Player player) {
        CarnageTyrant card = new CarnageTyrant();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
