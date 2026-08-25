package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GreaterWerewolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilverBolt.class, CrawWurm.class, GreaterWerewolf.class})
class SilverBoltTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a creature and sacrifices Silver Bolt")
    void dealsDamageToCreature() {
        Permanent bolt = addReadyBolt();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        addMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Silver Bolt");
        harness.assertInGraveyard(player1, "Silver Bolt");
        assertThat(bolt.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Destroys a Werewolf that was dealt damage")
    void destroysWerewolfThatWasDealtDamage() {
        addReadyBolt();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterWerewolf());
        addMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Greater Werewolf");
        harness.assertInGraveyard(player2, "Greater Werewolf");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyBolt();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SilverBolt());
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private Permanent addReadyBolt() {
        Permanent bolt = harness.addToBattlefieldAndReturn(player1, new SilverBolt());
        bolt.setSummoningSick(false);
        return bolt;
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
