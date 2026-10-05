package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.ArchersParapet;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KheruDreadmaw.class, ArchersParapet.class})
class KheruDreadmawTest extends BaseCardTest {

    @Test
    void sacrificesAnotherCreatureAndGainsItsToughness() {
        addDreadmawReady(player1);
        harness.addToBattlefield(player1, new ArchersParapet());
        harness.setLife(player1, 10);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertInGraveyard(player1, "Archers' Parapet");
    }

    @Test
    void cannotSacrificeKheruDreadmawItself() {
        addDreadmawReady(player1);
        addActivationMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addDreadmawReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new KheruDreadmaw());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }

    @Test
    void usesModifiedToughnessWithoutSubtractingMarkedDamage() {
        addDreadmawReady(player1);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ArchersParapet());
        sacrifice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        sacrifice.setMarkedDamage(3);
        harness.setLife(player1, 10);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Archers' Parapet");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 17);
    }

    @Test
    void chosenCreatureDeterminesLifeGainWhenMultipleCreaturesAreAvailable() {
        addDreadmawReady(player1);
        harness.addToBattlefield(player1, new KheruDreadmaw());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ArchersParapet());
        harness.setLife(player1, 10);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertInGraveyard(player1, "Archers' Parapet");
        harness.assertOnBattlefield(player1, "Kheru Dreadmaw");
    }

    @Test
    void canActivateWhileSummoningSickAndTapped() {
        Permanent dreadmaw = harness.addToBattlefieldAndReturn(player1, new KheruDreadmaw());
        dreadmaw.setSummoningSick(true);
        dreadmaw.setTapped(true);
        harness.addToBattlefield(player1, new ArchersParapet());
        harness.setLife(player1, 10);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
    }

    @Test
    void cannotSacrificeAnOpponentsCreature() {
        addDreadmawReady(player1);
        harness.addToBattlefield(player2, new ArchersParapet());
        addActivationMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Archers' Parapet");
    }
}
