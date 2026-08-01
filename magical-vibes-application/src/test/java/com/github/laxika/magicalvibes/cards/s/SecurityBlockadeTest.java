package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityBlockadeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a 2/2 white Knight token with vigilance for the Aura's controller")
    void entryCreatesKnightToken() {
        UUID landId = castBlockadeOn(player1);

        assertThat(landId).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Knight"))
                .singleElement()
                .satisfies(knight -> {
                    assertThat(knight.getCard().getPower()).isEqualTo(2);
                    assertThat(knight.getCard().getToughness()).isEqualTo(2);
                    assertThat(knight.getCard().isToken()).isTrue();
                    assertThat(knight.getCard().getKeywords()).contains(Keyword.VIGILANCE);
                });
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Knight"));
    }

    @Test
    @DisplayName("Enchanted land's granted ability prevents the next 1 damage to the land's controller")
    void grantedAbilityPreventsDamageToLandController() {
        harness.setLife(player2, 20);
        UUID landId = castBlockadeOn(player2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int landIndex = indexOf(player2, landId);
        harness.activateAbility(player2, landIndex, 0, null, null);
        harness.passBothPriorities();

        Permanent land = gd.playerBattlefields.get(player2.getId()).get(landIndex);
        assertThat(land.isTapped()).isTrue();

        // Opponent attacks the shielded player with a 2/2: 1 prevented, 1 gets through.
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot enchant a creature")
    void cannotEnchantCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new SecurityBlockade()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bearId))
                .isInstanceOf(IllegalStateException.class);
    }

    /** Casts Security Blockade from player1's hand onto a fresh Forest controlled by {@code landOwner}. */
    private UUID castBlockadeOn(Player landOwner) {
        harness.addToBattlefield(landOwner, new Forest());
        UUID landId = harness.getPermanentId(landOwner, "Forest");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SecurityBlockade()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, landId);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return landId;
    }

    private int indexOf(Player player, UUID permanentId) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getId().equals(permanentId)) {
                return i;
            }
        }
        throw new IllegalStateException("Permanent not on battlefield");
    }
}
