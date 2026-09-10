package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CityOfTraitors;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavenousBaboons.class, CityOfTraitors.class, Forest.class})
class RavenousBaboonsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys the target nonbasic land")
    void etbDestroysTargetNonbasicLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CityOfTraitors());
        harness.setHand(player1, List.of(new RavenousBaboons()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0, 0, target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ravenous Baboons");
        harness.assertNotOnBattlefield(player2, "City of Traitors");
        harness.assertInGraveyard(player2, "City of Traitors");
    }

    @Test
    @DisplayName("ETB trigger targets the chosen nonbasic land")
    void etbTriggerTargetsChosenNonbasicLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CityOfTraitors());
        harness.setHand(player1, List.of(new RavenousBaboons()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Basic lands are illegal ETB targets")
    void basicLandsAreIllegalTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new RavenousBaboons()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonbasic land");
    }

    @Test
    @DisplayName("No ETB trigger is put on the stack when only basic lands exist")
    void noTriggerWhenOnlyBasicLandsExist() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new RavenousBaboons()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ravenous Baboons");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB does nothing if its target leaves before resolution")
    void etbDoesNothingIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CityOfTraitors());
        harness.setHand(player1, List.of(new RavenousBaboons()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ravenous Baboons");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(log -> log.contains("fizzles"));
    }
}
