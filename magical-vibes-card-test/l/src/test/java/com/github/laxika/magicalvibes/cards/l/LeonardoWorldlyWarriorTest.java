package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AcidicSlime;
import com.github.laxika.magicalvibes.cards.e.ExoticOrchard;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeonardoWorldlyWarrior.class, AcidicSlime.class, ExoticOrchard.class})
class LeonardoWorldlyWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for creatures reduces the generic mana cost")
    void affinityForCreaturesReducesGenericCost() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new AcidicSlime());
        }
        harness.setHand(player1, List.of(new LeonardoWorldlyWarrior()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only creatures controlled by the spell's controller")
    void affinityCountsOnlyControlledCreatures() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player2, new AcidicSlime());
        }
        harness.setHand(player1, List.of(new LeonardoWorldlyWarrior()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Double strike deals combat damage in both combat damage steps")
    void doubleStrikeDealsCombatDamageTwice() {
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new LeonardoWorldlyWarrior());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }
    @Test
    @DisplayName("Affinity does not count noncreature permanents")
    void affinityDoesNotCountLands() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new ExoticOrchard());
        }
        harness.setHand(player1, List.of(new LeonardoWorldlyWarrior()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity counts tapped creatures and cannot reduce colored mana")
    void tappedCreaturesReduceOnlyGenericMana() {
        for (int i = 0; i < 8; i++) {
            Permanent creature = harness.addToBattlefieldAndReturn(player1, new AcidicSlime());
            creature.tap();
        }
        harness.setHand(player1, List.of(new LeonardoWorldlyWarrior()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }
}
