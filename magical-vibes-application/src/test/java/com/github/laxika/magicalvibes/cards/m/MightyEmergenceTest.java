package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MightyEmergenceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two +1/+1 counters on a power-5+ creature that enters when accepted")
    void putsCountersOnBigCreature() {
        harness.addToBattlefield(player1, new MightyEmergence());

        harness.setHand(player1, List.of(new AvatarOfMight())); // 8/8
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Avatar of Might

        // Enter trigger goes on stack — resolve it to get the may prompt, then accept.
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(findAvatar(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the trigger leaves the creature without counters")
    void declineLeavesNoCounters() {
        harness.addToBattlefield(player1, new MightyEmergence());

        harness.setHand(player1, List.of(new AvatarOfMight()));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Avatar of Might

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(findAvatar(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger for a creature you control with power less than 5")
    void doesNotTriggerForSmallCreature() {
        harness.addToBattlefield(player1, new MightyEmergence());

        harness.setHand(player1, List.of(new AirElemental())); // 4/4
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Air Elemental

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's power-5+ creature")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new MightyEmergence());
        harness.setHand(player1, List.of());

        harness.setHand(player2, List.of(new AvatarOfMight()));
        harness.addMana(player2, ManaColor.GREEN, 8);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve opponent's Avatar of Might

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent findAvatar(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Avatar of Might"))
                .findFirst().orElseThrow();
    }
}
