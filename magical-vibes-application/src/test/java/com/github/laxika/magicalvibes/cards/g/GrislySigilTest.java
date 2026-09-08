package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrislySigil.class, AirElemental.class, GrizzlyBears.class, Island.class, LightningBolt.class})
class GrislySigilTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage and gains 1 life against an undamaged target")
    void usesBaseEffectAgainstUndamagedTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new GrislySigil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Deals 3 damage and gains 3 life when the target was dealt noncombat damage")
    void usesUpgradedEffectAfterNoncombatDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new LightningBolt(), new GrislySigil()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(6);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Casualty copy resolves first and upgrades the original spell")
    void casualtyCopyUpgradesOriginalSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent casualty = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrislySigil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), casualty.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.setHand(player1, List.of(new GrislySigil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }
}
