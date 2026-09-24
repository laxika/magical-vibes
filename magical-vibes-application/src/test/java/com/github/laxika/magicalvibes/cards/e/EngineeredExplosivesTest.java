package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AuriokChampion;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.p.ParadiseMantle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EngineeredExplosives.class, AuriokChampion.class, ImprisonedInTheMoon.class,
        ParadiseMantle.class, Forest.class})
class EngineeredExplosivesTest extends BaseCardTest {

    @Test
    void sunburstPutsOneChargeCounterForEachColorSpent() {
        harness.setHand(player1, List.of(new EngineeredExplosives()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castArtifact(player1, 0, 3);
        harness.passBothPriorities();

        Permanent explosives = findPermanent(player1, "Engineered Explosives");
        assertThat(explosives.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void sunburstCountsEachColorOnlyOnce() {
        harness.setHand(player1, List.of(new EngineeredExplosives()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castArtifact(player1, 0, 3);
        harness.passBothPriorities();

        Permanent explosives = findPermanent(player1, "Engineered Explosives");
        assertThat(explosives.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void sacrificeDestroysMatchingNonlandPermanents() {
        Permanent explosives = harness.addToBattlefieldAndReturn(player1, new EngineeredExplosives());
        explosives.setCounterCount(CounterType.CHARGE, 2);
        harness.addToBattlefield(player1, new AuriokChampion());
        harness.addToBattlefield(player2, new AuriokChampion());
        harness.addToBattlefield(player2, new ParadiseMantle());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Engineered Explosives");
        harness.assertNotOnBattlefield(player1, "Auriok Champion");
        harness.assertNotOnBattlefield(player2, "Auriok Champion");
        harness.assertOnBattlefield(player2, "Paradise Mantle");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void doesNotDestroyPermanentThatHasBecomeALand() {
        Permanent transformedCreature = harness.addToBattlefieldAndReturn(player2, new AuriokChampion());
        harness.setHand(player1, List.of(new ImprisonedInTheMoon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, transformedCreature.getId());
        harness.passBothPriorities();

        Permanent explosives = harness.addToBattlefieldAndReturn(player1, new EngineeredExplosives());
        explosives.setCounterCount(CounterType.CHARGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Engineered Explosives");
        harness.assertOnBattlefield(player2, "Auriok Champion");
    }

    @Test
    void colorlessSunburstDoesNotDestroyNonzeroManaValuePermanents() {
        harness.setHand(player1, List.of(new EngineeredExplosives()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0, 2);
        harness.passBothPriorities();

        Permanent explosives = findPermanent(player1, "Engineered Explosives");
        assertThat(explosives.getCounterCount(CounterType.CHARGE)).isZero();
        harness.addToBattlefield(player2, new AuriokChampion());
        harness.addToBattlefield(player2, new ParadiseMantle());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Auriok Champion");
        harness.assertNotOnBattlefield(player2, "Paradise Mantle");
    }
}
