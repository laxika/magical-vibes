package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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
        harness.setHand(player1, List.of(new AzogMoriasRuin()));
        addManaForAzog();

        harness.castCreature(player1, 0);
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
