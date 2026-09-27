package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BenalishLancer;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelicShield.class, BenalishLancer.class, Mountain.class})
class AngelicShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +0/+1")
    void buffsOwnCreatures() {
        harness.addToBattlefield(player1, new AngelicShield());
        harness.addToBattlefield(player1, new BenalishLancer());

        Permanent lancer = findPermanent(player1, "Benalish Lancer");

        assertThat(gqs.getEffectivePower(gd, lancer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lancer)).isEqualTo(3);
    }

    @Test
    @DisplayName("Angelic Shield does not buff creatures an opponent controls")
    void doesNotBuffOpponentCreatures() {
        harness.addToBattlefield(player1, new AngelicShield());
        harness.addToBattlefield(player2, new BenalishLancer());

        Permanent lancer = findPermanent(player2, "Benalish Lancer");

        assertThat(gqs.getEffectivePower(gd, lancer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lancer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing Angelic Shield returns any target creature to its owner's hand")
    void sacrificesAndBouncesTargetCreature() {
        harness.addToBattlefield(player1, new AngelicShield());
        harness.addToBattlefield(player2, new BenalishLancer());

        Permanent lancer = findPermanent(player2, "Benalish Lancer");
        harness.activateAbility(player1, 0, null, lancer.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Angelic Shield");
        harness.assertInGraveyard(player1, "Angelic Shield");
        harness.assertNotOnBattlefield(player2, "Benalish Lancer");
        harness.assertInHand(player2, "Benalish Lancer");
    }

    @Test
    @DisplayName("Sacrificing Angelic Shield removes its boost from your creatures")
    void sacrificingShieldRemovesItsStaticBoost() {
        harness.addToBattlefield(player1, new AngelicShield());
        harness.addToBattlefield(player1, new BenalishLancer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BenalishLancer());
        Permanent ownLancer = findPermanent(player1, "Benalish Lancer");

        assertThat(gqs.getEffectiveToughness(gd, ownLancer)).isEqualTo(3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, ownLancer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns a controlled creature to its owner's hand")
    void returnsControlledCreatureToItsOwnersHand() {
        harness.addToBattlefield(player1, new AngelicShield());
        BenalishLancer ownedLancer = new BenalishLancer();
        ownedLancer.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, ownedLancer);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Benalish Lancer");
        harness.assertInHand(player1, "Benalish Lancer");
        harness.assertNotInHand(player2, "Benalish Lancer");
    }

    @Test
    @DisplayName("The activated ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new AngelicShield());
        harness.addToBattlefield(player2, new Mountain());

        Permanent mountain = findPermanent(player2, "Mountain");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
