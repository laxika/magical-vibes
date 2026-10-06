package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BurlyBreaker;
import com.github.laxika.magicalvibes.cards.d.DawnhartRejuvenator;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilverBolt.class, DawnhartRejuvenator.class, BurlyBreaker.class})
class SilverBoltTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a creature and sacrifices Silver Bolt")
    void dealsDamageToCreature() {
        Permanent bolt = addReadyBolt();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DawnhartRejuvenator());
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
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BurlyBreaker());
        addMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Burly Breaker");
        harness.assertInGraveyard(player1, "Burly Breaker");
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

    @Test
    @DisplayName("Fully prevented damage does not destroy a Werewolf")
    void fullyPreventedDamageDoesNotDestroyWerewolf() {
        addReadyBolt();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BurlyBreaker());
        target.setDamagePreventionShield(3);
        addMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Burly Breaker");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Even partially prevented damage destroys a Werewolf")
    void partiallyPreventedDamageDestroysWerewolf() {
        addReadyBolt();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BurlyBreaker());
        target.setDamagePreventionShield(2);
        addMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Burly Breaker");
        harness.assertInGraveyard(player1, "Burly Breaker");
    }

    @Test
    @DisplayName("An indestructible Werewolf survives with damage marked")
    void indestructibleWerewolfSurvives() {
        addReadyBolt();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BurlyBreaker());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        addMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Burly Breaker");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Silver Bolt is sacrificed before its ability resolves")
    void sacrificeIsPaidAtActivation() {
        addReadyBolt();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DawnhartRejuvenator());
        addMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Silver Bolt");
        harness.assertInGraveyard(player1, "Silver Bolt");
        assertThat(target.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
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
