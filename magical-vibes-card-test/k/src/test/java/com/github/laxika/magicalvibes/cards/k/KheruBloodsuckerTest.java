package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DisownedAncestor;
import com.github.laxika.magicalvibes.cards.m.MurderousCut;
import com.github.laxika.magicalvibes.cards.w.WallOfEssence;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KheruBloodsucker.class, GrizzlyBears.class, WallOfEssence.class,
        DisownedAncestor.class, MurderousCut.class})
class KheruBloodsuckerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature with toughness 4 or greater drains each opponent and gains life")
    void sacrificingLargeCreatureDrainsOpponents() {
        Permanent bloodsucker = addKheruReady(player1);
        harness.addToBattlefield(player1, new WallOfEssence());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bloodsucker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Sacrificing a creature with toughness less than 4 does not drain or gain life")
    void sacrificingSmallCreatureDoesNotDrain() {
        Permanent bloodsucker = addKheruReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bloodsucker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The activated ability cannot sacrifice Kheru Bloodsucker itself")
    void activatedAbilityRequiresAnotherCreature() {
        addKheruReady(player1);
        addActivationMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({KheruBloodsucker.class, MurderousCut.class})
    void ownDeathAtFourToughnessDrains() {
        Permanent bloodsucker = harness.addToBattlefieldAndReturn(player1, new KheruBloodsucker());
        bloodsucker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        destroyWithMurderousCut(bloodsucker);

        harness.assertInGraveyard(player1, "Kheru Bloodsucker");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @CardUsed({KheruBloodsucker.class, MurderousCut.class})
    void ownDeathBelowFourToughnessDoesNotDrain() {
        Permanent bloodsucker = harness.addToBattlefieldAndReturn(player1, new KheruBloodsucker());
        bloodsucker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        destroyWithMurderousCut(bloodsucker);

        harness.assertInGraveyard(player1, "Kheru Bloodsucker");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({KheruBloodsucker.class, DisownedAncestor.class, MurderousCut.class})
    void destructionOfLargeAllyDrains() {
        harness.addToBattlefield(player1, new KheruBloodsucker());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new DisownedAncestor());
        destroyWithMurderousCut(ally);

        harness.assertInGraveyard(player1, "Disowned Ancestor");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @CardUsed({KheruBloodsucker.class, DisownedAncestor.class, MurderousCut.class})
    void destructionOfOpponentsLargeCreatureDoesNotDrain() {
        harness.addToBattlefield(player1, new KheruBloodsucker());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DisownedAncestor());
        destroyWithMurderousCut(creature);

        harness.assertInGraveyard(player2, "Disowned Ancestor");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void destroyWithMurderousCut(Permanent target) {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new MurderousCut()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addKheruReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new KheruBloodsucker());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.BLACK, 1);
    }
}
