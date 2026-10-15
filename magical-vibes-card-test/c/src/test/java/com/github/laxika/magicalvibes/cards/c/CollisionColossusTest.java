package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.cards.s.SenateGriffin;
import com.github.laxika.magicalvibes.cards.s.Scorchmark;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CollisionColossus.class, SenateGriffin.class, SauroformHybrid.class, Scorchmark.class})
class CollisionColossusTest extends BaseCardTest {

    @Test
    @DisplayName("Collision deals 6 damage to a creature with flying")
    void collisionDealsSixDamageToFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SenateGriffin());

        harness.setHand(player1, List.of(new CollisionColossus()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(6);
        harness.assertNotOnBattlefield(player2, "Senate Griffin");
        harness.assertInGraveyard(player2, "Senate Griffin");
    }

    @Test
    @DisplayName("Collision cannot target a creature without flying")
    void collisionCannotTargetNonFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());

        harness.setHand(player1, List.of(new CollisionColossus()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Colossus gives a creature +4/+2 and trample until end of turn")
    void colossusBoostsCreatureAndGrantsTrample() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());

        harness.setHand(player1, List.of(new CollisionColossus()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalInstant(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void collisionCanBePaidWithGreenMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SenateGriffin());
        harness.setHand(player1, List.of(new CollisionColossus()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(6);
        harness.assertInGraveyard(player1, "Senate Griffin");
    }

    @Test
    void colossusRequiresBothRedAndGreenMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        harness.setHand(player1, List.of(new CollisionColossus()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void colossusDoesNotAffectAnotherCreatureWhenItsTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        harness.setHand(player1, List.of(new CollisionColossus()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Scorchmark()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, 1, List.of(target.getId()));
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target).contains(other);
        harness.passBothPriorities();

        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(2);
        assertThat(other.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Collision // Colossus");
    }
}
