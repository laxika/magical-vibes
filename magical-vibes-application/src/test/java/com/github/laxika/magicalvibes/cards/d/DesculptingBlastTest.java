package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesculptingBlast.class, EkunduGriffin.class, Forest.class, GrizzlyBears.class})
class DesculptingBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a nonland permanent and creates a Drone if it was attacking")
    void returnsPermanentAndCreatesDroneIfItWasAttacking() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setAttacking(true);

        castAt(target);

        harness.assertInHand(player2, "Grizzly Bears");
        Permanent drone = findPermanent(player1, "Drone");
        assertThat(drone.getCard().getColor()).isNull();
        assertThat(drone.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(drone.getCard().getSubtypes()).contains(CardSubtype.DRONE);
        assertThat(gqs.hasKeyword(gd, drone, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, drone)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drone)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create a Drone if the returned permanent was not attacking")
    void doesNotCreateDroneIfItWasNotAttacking() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castAt(target);

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Drone")).isEmpty();
    }

    @Test
    @DisplayName("A created Drone can block flying creatures but not ground creatures")
    void droneCanBlockOnlyFlyingCreatures() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setAttacking(true);
        castAt(target);

        Permanent groundAttacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent flyingAttacker = addCreatureReady(player2, new EkunduGriffin());
        Permanent drone = findPermanent(player1, "Drone");

        declareAttackers(player2, List.of(0, 1));
        prepareDeclareBlockers(player2);

        int droneIndex = gd.playerBattlefields.get(player1.getId()).indexOf(drone);
        int groundAttackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(groundAttacker);
        int flyingAttackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(flyingAttacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(droneIndex, groundAttackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(droneIndex, flyingAttackerIndex)));
        assertThat(drone.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new DesculptingBlast()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }

    private void castAt(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DesculptingBlast()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
