package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AzogMoriasRuin.class, AirElemental.class, Forest.class, GrizzlyBears.class})
class AzogMoriasRuinTest extends BaseCardTest {

    @Test
    void destroysAnOpposingCreatureAndItsControllerAmassesGoblinsByPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castAzog(target.getId());

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
        Permanent army = findPermanent(player2, "Goblin Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(army.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN, CardSubtype.ARMY);
    }

    @Test
    void destroysYourCreatureAmassesAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        castAzog(target.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        assertThat(findPermanent(player1, "Goblin Army")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void addsCountersAndGoblinSubtypeToAnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castAzog(target.getId());

        assertThat(findPermanents(player2, "Goblin Army")).isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ARMY, CardSubtype.GOBLIN);
    }

    @Test
    void doesNothingWhenTheOptionalTargetIsDeclined() {
        harness.castFromHand(player1, new AzogMoriasRuin(), "{2}{B}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
        assertThat(findPermanents(player2, "Goblin Army")).isEmpty();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AzogMoriasRuin()));
        addManaForAzog();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature");
    }

    @Test
    void usesTheTargetsModifiedPowerBeforeDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 3);

        castAzog(target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanent(player2, "Goblin Army")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void amassesAndDrawsEvenWhenYourCreatureIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setLibrary(player1, List.of(new Forest()));

        castAzog(target.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        assertThat(findPermanent(player1, "Goblin Army")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void zeroPowerStillMakesAnExistingArmyAGoblinAndDraws() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setPowerModifier(-2);
        harness.setLibrary(player1, List.of(new Forest()));

        castAzog(target.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ARMY, CardSubtype.GOBLIN);
        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
    }

    @Test
    void theTargetsControllerChoosesWhichArmyReceivesCounters() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        second.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castAzog(target.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(first.getGrantedSubtypes()).doesNotContain(CardSubtype.GOBLIN);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(second.getGrantedSubtypes()).contains(CardSubtype.GOBLIN);
        assertThat(findPermanents(player2, "Goblin Army")).isEmpty();
    }

    private void castAzog(UUID targetId) {
        harness.setHand(player1, List.of(new AzogMoriasRuin()));
        addManaForAzog();
        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();
    }

    private void addManaForAzog() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
