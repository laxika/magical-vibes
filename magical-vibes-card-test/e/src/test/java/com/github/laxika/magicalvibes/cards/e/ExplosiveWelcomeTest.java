package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SpinedKarok;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExplosiveWelcome.class, SpinedKarok.class, Expel.class})
class ExplosiveWelcomeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 and 3 damage to different targets, then adds three red mana")
    void dealsDamageAndAddsMana() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ExplosiveWelcome()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castAndResolveInstant(player1, 0, List.of(player2.getId(), player1.getId()));

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot choose the same target twice")
    void requiresDifferentTargets() {
        harness.setHand(player1, List.of(new ExplosiveWelcome()));
        harness.addMana(player1, ManaColor.RED, 8);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotCastWithOnlyOneTarget() {
        harness.setHand(player1, List.of(new ExplosiveWelcome()));
        harness.addMana(player1, ManaColor.RED, 8);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dealsFiveAndThreeDamageToCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SpinedKarok());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SpinedKarok());
        harness.setHand(player1, List.of(new ExplosiveWelcome()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertNotOnBattlefield(player2, "Spined Karok");
        harness.assertInGraveyard(player2, "Spined Karok");
        harness.assertOnBattlefield(player1, "Spined Karok");
        assertThat(second.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void resolvesRemainingTargetAndAddsManaWhenOneTargetBecomesIllegal(boolean firstTargetRemoved) {
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new SpinedKarok());
        removed.tap();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ExplosiveWelcome()));
        harness.setHand(player2, List.of(new Expel()));
        harness.addMana(player1, ManaColor.RED, 8);
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, firstTargetRemoved
                ? List.of(removed.getId(), player2.getId())
                : List.of(player2.getId(), removed.getId()));
        harness.castAndResolveInstant(player2, 0, removed.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(firstTargetRemoved ? 17 : 15);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Explosive Welcome");
    }

    @Test
    void doesNotAddManaWhenBothTargetsBecomeIllegal() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SpinedKarok());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SpinedKarok());
        first.tap();
        second.tap();
        harness.setHand(player1, List.of(new ExplosiveWelcome()));
        harness.setHand(player2, List.of(new Expel(), new Expel()));
        harness.addMana(player1, ManaColor.RED, 8);
        harness.addMana(player2, ManaColor.WHITE, 6);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.castAndResolveInstant(player2, 0, first.getId());
        harness.castAndResolveInstant(player2, 0, second.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.assertInGraveyard(player1, "Explosive Welcome");
    }
}
