package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.ThunderscapeFamiliar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlingshotGoblin.class, SeaSnidd.class, ThunderscapeFamiliar.class, StarCompass.class})
class SlingshotGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a target blue creature")
    void dealsDamageToBlueCreature() {
        Permanent goblin = addCreatureReady(player1, new SlingshotGoblin());
        Permanent target = addCreatureReady(player2, new SeaSnidd());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(goblin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a nonblue creature")
    void cannotTargetNonblueCreature() {
        addCreatureReady(player1, new SlingshotGoblin());
        Permanent target = addCreatureReady(player2, new ThunderscapeFamiliar());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a blue creature it controls")
    void canTargetOwnBlueCreature() {
        addCreatureReady(player1, new SlingshotGoblin());
        Permanent target = addCreatureReady(player1, new SeaSnidd());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new SlingshotGoblin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StarCompass());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires red mana to activate")
    void requiresRedManaToActivate() {
        Permanent goblin = addCreatureReady(player1, new SlingshotGoblin());
        Permanent target = addCreatureReady(player2, new SeaSnidd());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(goblin.isTapped()).isFalse();
        assertThat(target.getMarkedDamage()).isZero();
    }
}
