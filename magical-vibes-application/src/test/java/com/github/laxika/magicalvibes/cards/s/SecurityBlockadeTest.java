package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.a.AnnihilatingFire;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SecurityBlockade.class, Forest.class, DrudgeBeetle.class, AnnihilatingFire.class, SunderingGrowth.class})
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
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot enchant a creature")
    void cannotEnchantCreature() {
        harness.addToBattlefield(player2, new DrudgeBeetle());
        UUID bearId = harness.getPermanentId(player2, "Drudge Beetle");
        harness.setHand(player1, List.of(new SecurityBlockade()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bearId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanting an opponent's land still creates the Knight for the Aura's controller")
    void opponentLandCreatesKnightForAuraController() {
        castBlockadeOn(player2);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Knight"))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Knight"));
    }

    @Test
    @DisplayName("The shield prevents only one point of noncombat damage and is then consumed")
    void shieldIsConsumedByFirstDamage() {
        harness.setLife(player1, 20);
        UUID landId = castBlockadeOn(player1);
        harness.activateAbility(player1, indexOf(player1, landId), 0, null, null);
        harness.passBothPriorities();

        castFireAt(player1);
        harness.assertLife(player1, 18);
        castFireAt(player1);
        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("A resolved shield survives removal of the Aura, but the land loses the granted ability")
    void removingAuraDoesNotRemoveResolvedShield() {
        harness.setLife(player1, 20);
        UUID landId = castBlockadeOn(player1);
        int landIndex = indexOf(player1, landId);
        harness.activateAbility(player1, landIndex, 0, null, null);
        harness.passBothPriorities();

        UUID auraId = harness.getPermanentId(player1, "Security Blockade");
        harness.setHand(player2, List.of(new SunderingGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, auraId);
        harness.assertInGraveyard(player1, "Security Blockade");

        gd.playerBattlefields.get(player1.getId()).get(landIndex).untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, landIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        castFireAt(player1);
        harness.assertLife(player1, 18);
    }

    private void castFireAt(Player target) {
        harness.setHand(player2, List.of(new AnnihilatingFire()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
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
