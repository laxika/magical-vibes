package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.cards.f.FoundryHelix;
import com.github.laxika.magicalvibes.cards.g.GeyadroneDihada;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlagStrider.class, Spellbook.class, FyndhornElves.class,
        OrnithopterOfParadise.class, GeyadroneDihada.class, FoundryHelix.class})
class SlagStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces the generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, List.of(new SlagStrider()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Ability sacrifices an artifact and deals 1 damage to a player")
    void sacrificesArtifactAndDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new SlagStrider());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Ability deals 1 damage to a target creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player1, new SlagStrider());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new FyndhornElves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Fyndhorn Elves"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fyndhorn Elves");
    }

    @Test
    @DisplayName("Cannot activate ability without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        harness.addToBattlefield(player1, new SlagStrider());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
    }

    @Test
    void affinityDoesNotCountOpponentsArtifactsOrNonartifactCreatures() {
        harness.addToBattlefield(player2, new OrnithopterOfParadise());
        harness.addToBattlefield(player1, new SlagStrider());
        harness.setHand(player1, List.of(new SlagStrider(), new OrnithopterOfParadise()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void excessArtifactsReduceGenericCostToZero() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new OrnithopterOfParadise());
        }
        harness.setHand(player1, List.of(new SlagStrider()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void excessArtifactsDoNotReduceColoredCost() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new OrnithopterOfParadise());
        }
        harness.setHand(player1, List.of(new SlagStrider()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Slag Strider");
    }

    @Test
    void cannotSacrificeOpponentsArtifact() {
        harness.addToBattlefield(player1, new SlagStrider());
        harness.addToBattlefield(player2, new OrnithopterOfParadise());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");

        harness.assertOnBattlefield(player2, "Ornithopter of Paradise");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutManaEvenWithArtifact() {
        harness.addToBattlefield(player1, new SlagStrider());
        harness.addToBattlefield(player1, new OrnithopterOfParadise());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Ornithopter of Paradise");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedSummoningSickSourceCanActivateRepeatedlyAndSacrificeTappedArtifactCreatures() {
        var strider = harness.addToBattlefieldAndReturn(player1, new SlagStrider());
        strider.setTapped(true);
        strider.setSummoningSick(true);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        for (int i = 0; i < 2; i++) {
            var artifact = harness.addToBattlefieldAndReturn(player1, new OrnithopterOfParadise());
            artifact.setTapped(true);
            harness.activateAbility(player1, 0, null, player2.getId());
            harness.assertNotOnBattlefield(player1, "Ornithopter of Paradise");
            harness.assertLife(player2, 20 - i);
            harness.passBothPriorities();
            harness.assertLife(player2, 19 - i);
        }

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void dealsDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new SlagStrider());
        harness.addToBattlefield(player1, new OrnithopterOfParadise());
        var dihada = harness.addToBattlefieldAndReturn(player2, new GeyadroneDihada());
        dihada.setCounterCount(CounterType.LOYALTY, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, dihada.getId());
        harness.passBothPriorities();

        assertThat(dihada.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void cannotTargetNoncreatureArtifact() {
        harness.addToBattlefield(player1, new SlagStrider());
        harness.addToBattlefield(player1, new OrnithopterOfParadise());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Spellbook")))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Ornithopter of Paradise");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetTheArtifactCreatureSacrificedToPayTheCost() {
        harness.addToBattlefield(player1, new SlagStrider());
        var artifact = harness.addToBattlefieldAndReturn(player1, new OrnithopterOfParadise());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, artifact.getId());

        harness.assertInGraveyard(player1, "Ornithopter of Paradise");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertOnBattlefield(player1, "Slag Strider");
    }

    @Test
    void abilityStillDealsDamageAfterSourceLeavesBattlefield() {
        var strider = harness.addToBattlefieldAndReturn(player1, new SlagStrider());
        harness.addToBattlefield(player1, new OrnithopterOfParadise());
        var sacrifice = harness.addToBattlefieldAndReturn(player2, new SlagStrider());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new FoundryHelix()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.castInstantWithSacrifice(player2, 0, strider.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Slag Strider");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }
}
