package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeosurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Geosurge has correct spell effect")
    void hasCorrectEffect() {
        Geosurge card = new Geosurge();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(AwardRestrictedManaEffect.class);
        AwardRestrictedManaEffect effect =
                (AwardRestrictedManaEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.color()).isEqualTo(ManaColor.RED);
        assertThat(effect.amount()).isEqualTo(7);
        assertThat(effect.allowedSpellTypes()).containsExactlyInAnyOrder(CardType.CREATURE, CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Geosurge adds 7 restricted red mana on resolution")
    void addsRestrictedRedMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Geosurge()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getRestrictedRed()).isEqualTo(7);
    }

    @Test
    @DisplayName("Restricted red mana can be used to cast creature spells")
    void restrictedManaCanCastCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Cast Geosurge first to get the restricted mana
        harness.setHand(player1, List.of(new Geosurge()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getRestrictedRed()).isEqualTo(7);

        // Air Elemental costs {3}{U}{U} — we need 2 blue + 3 from restricted red
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        // 3 restricted red used for generic, 4 remaining
        assertThat(pool.getRestrictedRed()).isEqualTo(4);
    }

    @Test
    @DisplayName("Restricted red mana can be used to cast artifact spells")
    void restrictedManaCanCastArtifacts() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Cast Geosurge first
        harness.setHand(player1, List.of(new Geosurge()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getRestrictedRed()).isEqualTo(7);

        // Copper Myr costs {2} — castable with restricted red for generic
        harness.setHand(player1, List.of(new CopperMyr()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(pool.getRestrictedRed()).isEqualTo(5);
    }

    @Test
    @DisplayName("Restricted red mana cannot be used to cast non-creature non-artifact spells")
    void restrictedManaCannotCastOtherSpells() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Give player only restricted red mana (plus enough to cast Geosurge)
        harness.setHand(player1, List.of(new Geosurge()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getRestrictedRed()).isEqualTo(7);

        // Shock costs {R} — it's an instant, not a creature or artifact
        harness.setHand(player1, List.of(new Shock()));
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted red mana can pay for colored red costs of creature spells")
    void restrictedRedPaysColoredRedCosts() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Directly set restricted mana in pool instead of casting Geosurge
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addRestrictedRed(7);

        // A creature that costs red mana — use Geosurge's restricted red
        // GoblinEliteInfantry costs {1}{R}
        var goblin = new GoblinEliteInfantry();
        harness.setHand(player1, List.of(goblin));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        // {1}{R} = 1 generic + 1 red colored, both from restricted pool
        assertThat(pool.getRestrictedRed()).isEqualTo(5);
    }

    @Test
    @DisplayName("Restricted red mana is cleared on mana pool clear")
    void restrictedManaIsCleared() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addRestrictedRed(7);

        pool.clear();

        assertThat(pool.getRestrictedRed()).isEqualTo(0);
    }
}
