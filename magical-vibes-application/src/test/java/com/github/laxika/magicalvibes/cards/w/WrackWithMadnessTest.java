package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WrackWithMadnessTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Wrack with Madness targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsItOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrackWithMadness()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Wrack with Madness");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Wrack with Madness kills a 2/2 when it deals 2 damage to itself")
    void killsCreatureWhenPowerIsLethal() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrackWithMadness()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Wrack with Madness leaves a 3/5 alive with 3 marked damage")
    void survivesWhenPowerIsBelowToughness() {
        harness.addToBattlefield(player2, new WallOfSwords());
        harness.setHand(player1, List.of(new WrackWithMadness()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Wall of Swords");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent wall = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wall of Swords"))
                .findFirst()
                .orElseThrow();
        assertThat(wall.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Wrack with Madness deals no damage when target has 0 power")
    void dealsNoDamageWhenPowerIsZero() {
        harness.addToBattlefield(player2, new WallOfVines());
        harness.setHand(player1, List.of(new WrackWithMadness()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Wall of Vines");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent wall = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wall of Vines"))
                .findFirst()
                .orElseThrow();
        assertThat(wall.getMarkedDamage()).isZero();
    }

    // ===== Illegal target — "target creature" can't be a land =====
    // Wrack with Madness carries no card-level target filter, so the effect's @ValidatesTarget
    // validator is the only thing that stops the single-targetId cast at a land.
    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // valid target so the spell is castable
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new WrackWithMadness()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID plainsId = harness.getPermanentId(player2, "Plains");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, plainsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Plains"));
    }

    // Step 4 (targeting unification): the UI/AI enumeration path (ValidTargetService) must judge a
    // candidate by the SAME logic as the single-targetId cast path. Before unification this filterless
    // "target creature" spell's structural any-target block did NOT fire (its effect is creature-only,
    // not "any target"), so the enumeration path offered a land that the cast path rejected. The shared
    // @ValidatesTarget validator now runs on both paths.
    @Test
    @DisplayName("Target enumeration excludes a land (same rule as the cast path)")
    void targetEnumerationExcludesLand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new WrackWithMadness()));

        GameData gd = harness.getGameData();
        Card wrack = gd.playerHands.get(player1.getId()).getFirst();
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID plainsId = harness.getPermanentId(player2, "Plains");

        var response = harness.getValidTargetService()
                .computeValidTargetsForSpell(gd, wrack, player1.getId(), null);

        assertThat(response.validPermanentIds()).contains(bearId).doesNotContain(plainsId);
    }

    @Test
    @DisplayName("Wrack with Madness fizzles when target is gone before resolution")
    void fizzlesWhenTargetLeavesBattlefield() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrackWithMadness()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);

        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Wrack with Madness"));
    }
}
