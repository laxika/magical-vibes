package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowHarrier;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProtectiveBubble.class, NamelessInversion.class, GoldmeadowHarrier.class, Mountain.class})
class ProtectiveBubbleTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Protective Bubble puts it on the stack targeting a creature")
    void castingPutsOnStack() {
        Permanent bears = addReadyCreature(player1);

        harness.setHand(player1, List.of(new ProtectiveBubble()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Protective Bubble attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = addReadyCreature(player1);

        harness.setHand(player1, List.of(new ProtectiveBubble()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Protective Bubble")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    // ===== Static grants: shroud + can't be blocked =====

    @Test
    @DisplayName("Enchanted creature has shroud and can't be blocked")
    void enchantedCreatureHasShroudAndCantBeBlocked() {
        Permanent bears = addReadyCreature(player1);
        attachBubble(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, bears)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature loses shroud and can't-be-blocked when Bubble is removed")
    void grantsRemovedWhenBubbleRemoved() {
        Permanent bears = addReadyCreature(player1);
        Permanent bubble = attachBubble(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, bears)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(bubble);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, bears)).isFalse();
    }

    // ===== Shroud blocks targeting =====

    @Test
    @DisplayName("Enchanted creature cannot be targeted by its controller (shroud)")
    void enchantedCreatureCannotBeTargeted() {
        Permanent bears = addReadyCreature(player1);
        attachBubble(bears);

        harness.setHand(player1, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, bears.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GoldmeadowHarrier());
    }

    private Permanent attachBubble(Permanent creature) {
        Permanent bubble = harness.addToBattlefieldAndReturn(player1, new ProtectiveBubble());
        bubble.setAttachedTo(creature.getId());
        return bubble;
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        // A creature must exist so the spell is playable; targeting the land is then rejected.
        harness.addToBattlefield(player2, new GoldmeadowHarrier());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new ProtectiveBubble()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature cannot be blocked")
    void enchantedCreatureCannotBeBlocked() {
        Permanent attacker = addReadyCreature(player1);
        attachBubble(attacker);
        Permanent blocker = addReadyCreature(player2);
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Bubble can enchant an opponent's creature and grants only that creature its abilities")
    void canEnchantOpponentsCreature() {
        Permanent target = addReadyCreature(player2);
        Permanent other = addReadyCreature(player1);
        harness.setHand(player1, List.of(new ProtectiveBubble()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Protective Bubble").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.hasKeyword(gd, target, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, target)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, other)).isFalse();
        harness.assertNotInGraveyard(player1, "Protective Bubble");
    }

    @Test
    @DisplayName("Shroud prevents an opponent's activated ability from targeting the enchanted creature")
    void shroudStopsOpponentsActivatedAbility() {
        Permanent target = addReadyCreature(player1);
        attachBubble(target);
        addReadyCreature(player2);
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Bubble goes to the graveyard if its target dies before it resolves")
    void targetDiesInResponse() {
        Permanent target = addReadyCreature(player1);
        harness.setHand(player1, List.of(new ProtectiveBubble()));
        harness.setHand(player2, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goldmeadow Harrier");
        harness.assertInGraveyard(player1, "Protective Bubble");
        harness.assertNotOnBattlefield(player1, "Protective Bubble");
        assertThat(gd.stack).isEmpty();
    }
}
