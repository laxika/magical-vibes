package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RampartSmasher;
import com.github.laxika.magicalvibes.cards.r.RovingKeep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SearingBarrage.class, RampartSmasher.class, RovingKeep.class, Forest.class})
class SearingBarrageTest extends BaseCardTest {

    @Test
    void dealsFiveDamageWithoutAdamant() {
        harness.addToBattlefield(player2, new RampartSmasher());
        harness.setHand(player1, List.of(new SearingBarrage()));
        addMana(4, 1);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Rampart Smasher");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Rampart Smasher");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void adamantAlsoDealsThreeDamageToTheCreatureController() {
        harness.addToBattlefield(player2, new RampartSmasher());
        harness.setHand(player1, List.of(new SearingBarrage()));
        addMana(2, 3);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Rampart Smasher");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Rampart Smasher");
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void cannotTargetANonCreaturePermanent() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new SearingBarrage()));
        addMana(4, 1);

        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void twoRedManaDoesNotSatisfyAdamant() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RovingKeep());
        harness.setHand(player1, List.of(new SearingBarrage()));
        addMana(3, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Roving Keep");
        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertLife(player2, 20);
    }

    @Test
    void fiveRedManaSatisfiesAdamantEvenWhenCreatureSurvives() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RovingKeep());
        harness.setHand(player1, List.of(new SearingBarrage()));
        addMana(0, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Roving Keep");
        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    void adamantDamagesCasterWhenTargetingOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RampartSmasher());
        harness.setHand(player1, List.of(new SearingBarrage()));
        addMana(2, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Rampart Smasher");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    void illegalTargetPreventsAdamantDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampartSmasher());
        harness.setHand(player1, List.of(new SearingBarrage()));
        harness.setHand(player2, List.of(new SearingBarrage()));
        addMana(2, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Rampart Smasher");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Searing Barrage");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void addMana(int colorless, int red) {
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.RED, red);
    }
}
