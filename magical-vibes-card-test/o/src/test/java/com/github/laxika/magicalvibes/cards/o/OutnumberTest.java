package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.EldraziDevastator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Outnumber.class, EldraziDevastator.class, OranRiefInvoker.class})
class OutnumberTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of creatures the caster controls")
    void dealsDamageEqualToControlledCreatures() {
        harness.addToBattlefield(player1, new OranRiefInvoker());
        harness.addToBattlefield(player1, new OranRiefInvoker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EldraziDevastator());
        harness.setHand(player1, List.of(new Outnumber()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Eldrazi Devastator");
    }

    @Test
    @DisplayName("Counts only creatures controlled by the spell's controller")
    void countsOnlyControllersCreatures() {
        harness.addToBattlefield(player1, new OranRiefInvoker());
        harness.addToBattlefield(player2, new OranRiefInvoker());
        harness.addToBattlefield(player2, new OranRiefInvoker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EldraziDevastator());
        harness.setHand(player1, List.of(new Outnumber()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts creatures at resolution")
    void countsCreaturesAtResolution() {
        harness.addToBattlefield(player1, new OranRiefInvoker());
        harness.addToBattlefield(player1, new OranRiefInvoker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EldraziDevastator());
        harness.setHand(player1, List.of(new Outnumber()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).removeLast();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Outnumber()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot target players");
    }

    @Test
    @DisplayName("Deals no damage when the controller has no creatures")
    void noCreaturesDealsNoDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EldraziDevastator());
        harness.setHand(player1, List.of(new Outnumber()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Eldrazi Devastator");
        harness.assertInGraveyard(player1, "Outnumber");
    }

    @Test
    @DisplayName("Can target an own creature and includes it in the count")
    void countsOwnTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EldraziDevastator());
        harness.addToBattlefield(player1, new OranRiefInvoker());
        harness.setHand(player1, List.of(new Outnumber()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Eldrazi Devastator");
    }

    @Test
    @DisplayName("Lethal damage puts the target in the graveyard")
    void lethalDamageKillsTarget() {
        harness.addToBattlefield(player1, new OranRiefInvoker());
        harness.addToBattlefield(player1, new OranRiefInvoker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OranRiefInvoker());
        harness.setHand(player1, List.of(new Outnumber()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Oran-Rief Invoker");
        harness.assertInGraveyard(player2, "Oran-Rief Invoker");
    }

    @Test
    @DisplayName("Does not damage another creature when its target leaves")
    void targetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new OranRiefInvoker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EldraziDevastator());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new EldraziDevastator());
        harness.setHand(player1, List.of(new Outnumber()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(other.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Outnumber");
    }
}
