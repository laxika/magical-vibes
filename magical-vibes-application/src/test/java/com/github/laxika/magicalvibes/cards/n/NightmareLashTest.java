package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NightmareLash.class, Frogmite.class, Swamp.class})
class NightmareLashTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each Swamp its equipment controller controls")
    void boostsPerSwamp() {
        Permanent creature = addCreatureReady(player1, new Frogmite());
        Permanent lash = addLash(player1);
        lash.setAttachedTo(creature.getId());

        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Swamp boost updates dynamically")
    void updatesDynamicallyWithSwampCount() {
        Permanent creature = addCreatureReady(player1, new Frogmite());
        Permanent lash = addLash(player1);
        lash.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);

        harness.addToBattlefield(player1, new Swamp());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Swamp"));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nightmare Lash does not boost an unequipped creature")
    void doesNotBoostUnequippedCreature() {
        Permanent creature = addCreatureReady(player1, new Frogmite());
        addLash(player1);
        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost counts Swamps controlled by the Equipment controller")
    void countsEquipmentControllersSwamps() {
        Permanent creature = addCreatureReady(player2, new Frogmite());
        Permanent lash = addLash(player1);
        lash.setAttachedTo(creature.getId());

        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equip pays 3 life and attaches Nightmare Lash to a creature you control")
    void equipPaysLifeAndAttaches() {
        Permanent lash = addLash(player1);
        Permanent creature = addCreatureReady(player1, new Frogmite());

        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);

        harness.passBothPriorities();

        assertThat(lash.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentCreature() {
        Permanent lash = addLash(player1);
        Permanent opponentCreature = addCreatureReady(player2, new Frogmite());

        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(lash.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated without enough life")
    void cannotEquipWithoutEnoughLife() {
        Permanent lash = addLash(player1);
        Permanent creature = addCreatureReady(player1, new Frogmite());

        harness.setLife(player1, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
        assertThat(gd.getLife(player1.getId())).isEqualTo(2);
        assertThat(lash.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip can only be activated as a sorcery")
    void equipIsSorcerySpeedOnly() {
        Permanent lash = addLash(player1);
        Permanent creature = addCreatureReady(player1, new Frogmite());

        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(lash.getAttachedTo()).isNull();
    }

    private Permanent addLash(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new NightmareLash());
    }
}
