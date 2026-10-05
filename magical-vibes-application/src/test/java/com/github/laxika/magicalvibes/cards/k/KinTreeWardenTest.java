package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BringLow;
import com.github.laxika.magicalvibes.cards.t.Throttle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KinTreeWarden.class, BringLow.class, Throttle.class})
class KinTreeWardenTest extends BaseCardTest {

    @Test
    void activatingRegenerationAbilityGrantsShield() {
        Permanent warden = addCreatureReady(player1, new KinTreeWarden());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(warden.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUpForGreen() {
        harness.setHand(player1, List.of(new KinTreeWarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent warden = findPermanent(player1, "Kin-Tree Warden");
        assertThat(warden.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(warden));
        harness.passBothPriorities();

        assertThat(warden.isFaceDown()).isFalse();
    }

    @Test
    void regenerationSavesSummoningSickCreatureFromLethalDamage() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new KinTreeWarden());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(warden.isTapped()).isFalse();
        harness.setHand(player1, List.of(new BringLow()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveInstant(player1, 0, warden.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(warden);
        assertThat(warden.isTapped()).isTrue();
        assertThat(warden.getMarkedDamage()).isZero();
        assertThat(warden.getRegenerationShield()).isZero();
    }

    @Test
    void regenerationDoesNotSaveCreatureWithNonpositiveToughness() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new KinTreeWarden());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Throttle()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player1, 0, warden.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(warden);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(warden.getCard());
    }

    @Test
    void regenerationBecomesAvailableImmediatelyAfterTurningFaceUp() {
        harness.setHand(player1, List.of(new KinTreeWarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent warden = findPermanent(player1, "Kin-Tree Warden");

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(warden.isFaceDown()).isFalse();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(warden.getRegenerationShield()).isEqualTo(1);
    }
}
