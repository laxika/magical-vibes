package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImpactResonance.class, Pyroclasm.class, GiantSpider.class, LightningBolt.class})
class ImpactResonanceTest extends BaseCardTest {

    @Test
    void usesTheGreatestDamageToOneRecipientNotTheSourceTotal() {
        harness.forceActivePlayer(player1);
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.setHand(player1, List.of(new ImpactResonance()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, Map.of(firstTarget.getId(), 1, secondTarget.getId(), 1));
        harness.passBothPriorities();

        assertThat(firstTarget.getMarkedDamage()).isEqualTo(3);
        assertThat(secondTarget.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void requiresAssignmentsToSumToTheCastTimeDamageValue() {
        harness.forceActivePlayer(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.setHand(player1, List.of(new ImpactResonance()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(target.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeCastWithoutTargetsWhenNoDamageHasBeenDealt() {
        harness.setHand(player1, List.of(new ImpactResonance()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, Map.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Impact Resonance");
    }

    @Test
    void canChooseZeroTargetsEvenWhenDamageHasBeenDealt() {
        harness.setHand(player1, List.of(new LightningBolt(), new ImpactResonance()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.castInstant(player1, 0, Map.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Impact Resonance");
        harness.assertLife(player2, 17);
    }

    @Test
    void keepsTheCastTimeDivisionWhenGreaterDamageIsDealtInResponse() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new Pyroclasm(), new ImpactResonance(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.castInstant(player1, 0, Map.of(target.getId(), 2));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void doesNotRedistributeDamageFromAnIllegalTarget() {
        harness.forceActivePlayer(player1);
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new Pyroclasm(), new ImpactResonance(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castInstant(player1, 0, Map.of(firstTarget.getId(), 1, secondTarget.getId(), 1));
        harness.castInstant(player1, 0, firstTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Giant Spider");
        assertThat(secondTarget.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondTarget);
    }

    @Test
    void usesDamageToAPlayerAndDoesNotCombineDifferentSources() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new ImpactResonance()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.castInstant(player1, 0, Map.of(target.getId(), 3));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 14);
    }

    @Test
    void rejectsPlayerTargetsAndZeroDamageAssignments() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent otherTarget = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new LightningBolt(), new ImpactResonance()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                Map.of(target.getId(), 3, otherTarget.getId(), 0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
