package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LanternSpiritTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Lantern Spirit has the return-to-hand activated ability")
    void hasCorrectAbility() {
        LanternSpirit card = new LanternSpirit();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{U}");
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst()).isInstanceOf(ReturnSelfToHandEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Lantern Spirit puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new LanternSpirit()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        gs.playCard(gd, player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lantern Spirit");
    }

    @Test
    @DisplayName("Resolving Lantern Spirit puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new LanternSpirit()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lantern Spirit"));
    }

    // ===== Activated ability: return to hand =====

    @Test
    @DisplayName("Activating {U} ability puts return-to-hand on the stack")
    void activateAbilityPutsOnStack() {
        harness.addToBattlefield(player1, new LanternSpirit());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating {U} ability returns Lantern Spirit to owner's hand")
    void activateAbilityReturnsToHand() {
        harness.addToBattlefield(player1, new LanternSpirit());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lantern Spirit"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Lantern Spirit"));
    }

    @Test
    @DisplayName("Lantern Spirit can be re-cast after returning to hand")
    void canRecastAfterBounce() {
        harness.addToBattlefield(player1, new LanternSpirit());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lantern Spirit"));

        // Re-cast it
        harness.addMana(player1, ManaColor.BLUE, 3);
        int spiritIndex = -1;
        var hand = gd.playerHands.get(player1.getId());
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals("Lantern Spirit")) {
                spiritIndex = i;
                break;
            }
        }
        gs.playCard(gd, player1, spiritIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lantern Spirit"));
    }

    @Test
    @DisplayName("Ability can be activated multiple times across re-casts")
    void canActivateMultipleTimesAcrossRecasts() {
        harness.addToBattlefield(player1, new LanternSpirit());

        // First bounce
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lantern Spirit"));

        // Re-cast
        harness.addMana(player1, ManaColor.BLUE, 3);
        int spiritIndex = -1;
        var hand = gd.playerHands.get(player1.getId());
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals("Lantern Spirit")) {
                spiritIndex = i;
                break;
            }
        }
        gs.playCard(gd, player1, spiritIndex, 0, null, null);
        harness.passBothPriorities();

        // Second bounce
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lantern Spirit"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Lantern Spirit"));
    }
}
