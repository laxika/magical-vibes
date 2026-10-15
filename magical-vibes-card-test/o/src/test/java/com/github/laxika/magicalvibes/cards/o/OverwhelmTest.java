package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.n.NullmageShepherd;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Overwhelm.class, NullmageShepherd.class})
class OverwhelmTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gives creatures you control +3/+3")
    void boostsAllOwnCreatures() {
        harness.addToBattlefield(player1, new NullmageShepherd());
        harness.addToBattlefield(player1, new NullmageShepherd());
        harness.castFromHand(player1, new Overwhelm(), "{5}{G}{G}");

        harness.passBothPriorities();

        for (Permanent p : gd.playerBattlefields.get(player1.getId())) {
            assertThat(p.getEffectivePower()).isEqualTo(5);
            assertThat(p.getEffectiveToughness()).isEqualTo(7);
        }
        harness.assertInGraveyard(player1, "Overwhelm");
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new NullmageShepherd());
        harness.addToBattlefield(player2, new NullmageShepherd());
        harness.castFromHand(player1, new Overwhelm(), "{5}{G}{G}");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getEffectivePower()).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new NullmageShepherd());
        harness.castFromHand(player1, new Overwhelm(), "{5}{G}{G}");

        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent shepherd = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shepherd.getEffectivePower()).isEqualTo(2);
        assertThat(shepherd.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost creatures entering after it resolves")
    void doesNotBoostCreaturesEnteringAfterResolution() {
        harness.castFromHand(player1, new Overwhelm(), "{5}{G}{G}");
        harness.passBothPriorities();

        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new NullmageShepherd());

        assertThat(lateCreature.getEffectivePower()).isEqualTo(2);
        assertThat(lateCreature.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Convoke taps creatures and reduces the mana needed to cast the spell")
    void castsWithConvoke() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new NullmageShepherd());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new NullmageShepherd());
        harness.setHand(player1, List.of(new Overwhelm()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(firstCreature.getEffectivePower()).isEqualTo(5);
        assertThat(secondCreature.getEffectivePower()).isEqualTo(5);
    }

    @Test
    @DisplayName("Seven summoning-sick green creatures can pay the entire cost with convoke")
    void castsEntirelyWithConvokeUsingSummoningSickCreatures() {
        List<Permanent> creatures = java.util.stream.IntStream.range(0, 7)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new NullmageShepherd()))
                .toList();
        creatures.forEach(creature -> creature.setSummoningSick(true));
        harness.setHand(player1, List.of(new Overwhelm()));

        harness.castInstantWithConvoke(player1, 0, List.of(),
                creatures.stream().map(Permanent::getId).toList());

        assertThat(creatures).allSatisfy(creature -> assertThat(creature.isTapped()).isTrue());
        harness.passBothPriorities();

        assertThat(creatures).allSatisfy(creature -> {
            assertThat(creature.getEffectivePower()).isEqualTo(5);
            assertThat(creature.getEffectiveToughness()).isEqualTo(7);
        });
        harness.assertInGraveyard(player1, "Overwhelm");
    }

    @Test
    @DisplayName("Creatures entering while Overwhelm is on the stack receive the boost")
    void boostsCreaturesEnteringBeforeResolution() {
        harness.castFromHand(player1, new Overwhelm(), "{5}{G}{G}");
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NullmageShepherd());

        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(5);
        assertThat(creature.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("A tapped creature cannot convoke Overwhelm")
    void cannotConvokeWithTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NullmageShepherd());
        creature.tap();
        harness.setHand(player1, List.of(new Overwhelm()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
    }
}
