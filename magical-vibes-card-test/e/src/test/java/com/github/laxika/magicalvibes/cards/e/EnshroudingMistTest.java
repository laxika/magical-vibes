package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnshroudingMist.class, GrizzlyBears.class, Shock.class})
class EnshroudingMistTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +1/+1 until end of turn")
    void boostsTargetCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EnshroudingMist()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("All damage that would be dealt to the target this turn is prevented")
    void preventsAllDamageToTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EnshroudingMist(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearId);

        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear).isNotNull();
        assertThat(bear.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A renowned target is untapped")
    void untapsRenownedTarget() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setRenowned(true);
        bear.tap();
        harness.setHand(player1, List.of(new EnshroudingMist()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A target that isn't renowned stays tapped")
    void doesNotUntapNonRenownedTarget() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.tap();
        harness.setHand(player1, List.of(new EnshroudingMist()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevention applies to repeated damage events and expires at cleanup")
    void preventionPersistsForMultipleEventsButExpires() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EnshroudingMist(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.castAndResolveInstant(player1, 0, bear.getId());
        assertThat(bear.getMarkedDamage()).isZero();
        harness.castAndResolveInstant(player1, 0, bear.getId());
        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(findPermanent(player1, "Grizzly Bears")).isSameAs(bear);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bear.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("An opposing renowned creature gets all effects while other creatures remain unprotected")
    void canProtectAndUntapOpposingRenownedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setRenowned(true);
        target.tap();
        harness.setHand(player1, List.of(new EnshroudingMist(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(target.getMarkedDamage()).isZero();

        harness.castAndResolveInstant(player1, 0, other.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target).doesNotContain(other);
    }
}
