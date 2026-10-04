package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArashinCleric;
import com.github.laxika.magicalvibes.cards.w.WildSlash;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HoodedAssassin.class, ArashinCleric.class, WildSlash.class})
class HoodedAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Counter mode puts a +1/+1 counter on Hooded Assassin")
    void counterModePutsCounterOnItself() {
        castAssassin(0, null);

        Permanent assassin = findPermanent(player1, "Hooded Assassin");
        assertThat(assassin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, assassin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, assassin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Destroy mode destroys a creature dealt damage this turn")
    void destroyModeDestroysDamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        damageTarget(target);

        castAssassin(1, target.getId());

        harness.assertNotOnBattlefield(player2, "Arashin Cleric");
        harness.assertInGraveyard(player2, "Arashin Cleric");
        assertThat(findPermanent(player1, "Hooded Assassin")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Destroy mode rejects a creature not dealt damage this turn")
    void destroyModeRejectsUndamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        Permanent damaged = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        damageTarget(damaged);

        harness.setHand(player1, List.of(new HoodedAssassin()));
        addAssassinMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Destroy target creature that was dealt damage this turn");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAssassin(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new HoodedAssassin()));
        addAssassinMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        chooseMode(mode, targetId);
        harness.passBothPriorities();
    }

    private void chooseMode(int mode, java.util.UUID targetId) {
        harness.handleListChoice(player1, mode == 0
                ? "Put a +1/+1 counter on this creature"
                : "Destroy target creature that was dealt damage this turn");
        if (targetId != null) {
            harness.handlePermanentChosen(player1, targetId);
        }
    }

    @Test
    void destroyModeCanTargetOwnDamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArashinCleric());
        damageTarget(target);

        castAssassin(1, target.getId());

        harness.assertNotOnBattlefield(player1, "Arashin Cleric");
        harness.assertInGraveyard(player1, "Arashin Cleric");
    }

    @Test
    void canChooseDestroyModeWhenEnteringWithoutBeingCast() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        damageTarget(target);

        harness.enterBattlefieldAndReturn(player1, new HoodedAssassin());
        harness.passPriority(player1);
        chooseMode(1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Arashin Cleric");
    }

    @Test
    void canChooseCreatureDamagedWhileAssassinSpellIsOnStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        harness.setHand(player1, List.of(new HoodedAssassin(), new WildSlash()));
        addAssassinMana();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        chooseMode(1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Arashin Cleric");
    }

    @Test
    void destroyTriggerStillResolvesAfterAssassinDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        damageTarget(target);
        harness.setHand(player1, List.of(new HoodedAssassin(), new WildSlash()));
        addAssassinMana();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        chooseMode(1, target.getId());
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Hooded Assassin"));
        harness.assertInGraveyard(player1, "Hooded Assassin");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Arashin Cleric");
    }

    private void damageTarget(Permanent target) {
        harness.setHand(player1, List.of(new WildSlash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addAssassinMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
