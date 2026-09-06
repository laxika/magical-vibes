package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.ScarwoodBandits;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NiallSilvain.class, Squire.class, FountainOfYouth.class, ScarwoodBandits.class})
class NiallSilvainTest extends BaseCardTest {

    @Test
    @DisplayName("Regenerates a target creature and taps Niall Silvain")
    void regeneratesTargetCreature() {
        addCreatureReady(player1, new NiallSilvain());
        harness.addToBattlefield(player2, new Squire());
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Squire");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Niall Silvain");
        Permanent target = findPermanent(player2, "Squire");
        assertThat(source.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves a target creature from lethal combat damage")
    void regenerationShieldSavesTargetFromLethalCombatDamage() {
        addCreatureReady(player1, new NiallSilvain());
        Permanent target = addCreatureReady(player1, new Squire());
        Permanent attacker = addCreatureReady(player2, new ScarwoodBandits());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        target.setBlocking(true);
        target.addBlockingTarget(0);
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(findPermanent(player1, "Squire")).isSameAs(target);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new NiallSilvain());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
