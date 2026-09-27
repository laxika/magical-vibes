package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FirstVolley;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.i.IreOfKaminari;
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

@CardUsed({KitsunePalliator.class, GnarledMass.class, IreOfKaminari.class, FirstVolley.class})
class KitsunePalliatorTest extends BaseCardTest {

    @Test
    @DisplayName("Activating shields every creature and every player with a 1-damage shield")
    void shieldsEveryCreatureAndPlayer() {
        Permanent palliator = addReadyPalliator();
        Permanent friendly = addReadyCreature(player1);
        Permanent enemy = addReadyCreature(player2);

        activatePalliator(palliator);

        assertThat(palliator.getDamagePreventionShield()).isEqualTo(1);
        assertThat(friendly.getDamagePreventionShield()).isEqualTo(1);
        assertThat(enemy.getDamagePreventionShield()).isEqualTo(1);
        assertThat(gd.playerDamagePreventionShields.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the next 1 damage to a shielded creature is prevented")
    void preventsOnlyOneDamageToCreature() {
        Permanent palliator = addReadyPalliator();
        Permanent enemy = addReadyCreature(player2);

        activatePalliator(palliator);

        castIre(enemy.getId());

        assertThat(enemy.getMarkedDamage()).isEqualTo(1);
        assertThat(enemy.getDamagePreventionShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Only the next 1 damage to a shielded player is prevented")
    void preventsOnlyOneDamageToPlayer() {
        Permanent palliator = addReadyPalliator();
        int lifeBefore = gd.getLife(player2.getId());

        activatePalliator(palliator);

        castIre(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("A creature that enters after the ability resolves gets no shield")
    void laterCreatureIsNotShielded() {
        Permanent palliator = addReadyPalliator();

        activatePalliator(palliator);

        Permanent latecomer = addReadyCreature(player2);

        assertThat(latecomer.getDamagePreventionShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("The shields wear off at end of turn")
    void shieldsClearedAtEndOfTurn() {
        Permanent palliator = addReadyPalliator();

        activatePalliator(palliator);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(palliator.getDamagePreventionShield()).isEqualTo(0);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    private Permanent addReadyPalliator() {
        return addCreatureReady(player1, new KitsunePalliator());
    }

    private void activatePalliator(Permanent palliator) {
        harness.activateAbility(player1, indexOf(player1, palliator), 0, null, null);
        harness.passBothPriorities();
    }

    private void castIre(UUID targetId) {
        harness.setGraveyard(player1, List.of(new FirstVolley(), new FirstVolley()));
        harness.setHand(player1, List.of(new IreOfKaminari()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new GnarledMass());
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
