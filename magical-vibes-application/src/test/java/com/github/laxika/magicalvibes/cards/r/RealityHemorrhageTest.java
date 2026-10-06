package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NissaVoiceOfZendikar;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RealityHemorrhage.class, GrizzlyBears.class, NissaVoiceOfZendikar.class, PaladinEnVec.class})
class RealityHemorrhageTest extends BaseCardTest {

    @Test
    @DisplayName("Reality Hemorrhage deals 2 damage to target player")
    void deals2DamageToPlayer() {
        harness.setHand(player1, List.of(new RealityHemorrhage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Reality Hemorrhage deals 2 damage to target creature")
    void deals2DamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RealityHemorrhage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot cast Reality Hemorrhage without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new RealityHemorrhage()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Reality Hemorrhage can target its caster")
    void canDamageItsCaster() {
        harness.setHand(player1, List.of(new RealityHemorrhage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Reality Hemorrhage");
    }

    @Test
    @DisplayName("Reality Hemorrhage removes two loyalty counters from a planeswalker")
    void damagesPlaneswalker() {
        var nissa = harness.enterBattlefieldAndReturn(player2, new NissaVoiceOfZendikar());
        harness.setHand(player1, List.of(new RealityHemorrhage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, nissa.getId());

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Nissa, Voice of Zendikar");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Reality Hemorrhage marks damage without destroying a creature with three toughness")
    void nonlethalDamageLeavesCreatureOnBattlefield() {
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new RealityHemorrhage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Devoid allows Reality Hemorrhage to target and damage a creature with protection from red")
    void bypassesProtectionFromRed() {
        var paladin = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new RealityHemorrhage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, paladin.getId());

        harness.assertNotOnBattlefield(player2, "Paladin en-Vec");
        harness.assertInGraveyard(player2, "Paladin en-Vec");
    }
}
