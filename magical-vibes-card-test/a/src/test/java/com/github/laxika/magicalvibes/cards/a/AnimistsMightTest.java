package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.k.KarnScionOfUrza;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnimistsMight.class, AirElemental.class, GrizzlyBears.class, IsamaruHoundOfKonda.class,
        KarnScionOfUrza.class})
class AnimistsMightTest extends BaseCardTest {

    @Test
    @DisplayName("Deals twice the source creature's power to a creature")
    void dealsTwicePowerToCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new AnimistsMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.castAndResolveSorcery(player1, 0, List.of(source.getId(), targetId));

        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Can deal twice the source creature's power to a planeswalker")
    void dealsTwicePowerToPlaneswalker() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new KarnScionOfUrza());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new AnimistsMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(source.getId(), planeswalker.getId()));

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Requires the full cost when the first target is not legendary")
    void fullCostForNonlegendaryFirstTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new AnimistsMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires a creature you control first")
    void firstTargetMustBeControlledCreature() {
        Permanent opponentSource = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new AnimistsMight()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(opponentSource.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires a creature or planeswalker not controlled by the caster second")
    void secondTargetMustNotBeControlled() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new AnimistsMight()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(source.getId(), ownTarget.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("don't control");
    }

    @Test
    void nonlegendaryCreatureDealsDamageWhenFullCostIsPaid() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new AnimistsMight()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(source.getId(), target.getId()));

        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void usesPowerAtResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new AnimistsMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, List.of(source.getId(), target.getId()));

        source.setPowerModifier(-1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void dealsNoDamageWhenSourceChangesController() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new AnimistsMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, List.of(source.getId(), target.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerBattlefields.get(player2.getId()).add(source);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Animist's Might");
    }
}
