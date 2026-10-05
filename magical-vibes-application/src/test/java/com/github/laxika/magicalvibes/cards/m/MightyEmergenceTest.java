package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.a.AgonyWarp;
import com.github.laxika.magicalvibes.cards.c.CavernThoctar;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MightyEmergence.class, AvatarOfMight.class, AirElemental.class,
        Mosstodon.class, CavernThoctar.class, AgonyWarp.class, GloriousAnthem.class, Opalescence.class})
class MightyEmergenceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two +1/+1 counters on a power-5+ creature that enters when accepted")
    void putsCountersOnBigCreature() {
        harness.addToBattlefield(player1, new MightyEmergence());

        harness.castFromHand(player1, new AvatarOfMight(), "{6}{G}{G}");
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

        harness.castFromHand(player1, new AvatarOfMight(), "{6}{G}{G}");
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

        harness.castFromHand(player1, new AirElemental(), "{3}{U}{U}");
        harness.passBothPriorities(); // resolve Air Elemental

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's power-5+ creature")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new MightyEmergence());
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new AvatarOfMight(), "{6}{G}{G}");
        harness.passBothPriorities(); // resolve opponent's Avatar of Might

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggersForExactlyFivePower() {
        harness.addToBattlefield(player1, new MightyEmergence());
        harness.castFromHand(player1, new Mosstodon(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Mosstodon").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    void doesNotRecheckPowerWhenAbilityResolves() {
        harness.addToBattlefield(player1, new MightyEmergence());
        harness.castFromHand(player1, new CavernThoctar(), "{5}{G}");
        harness.passBothPriorities();
        Permanent creature = findPermanent(player1, "Cavern Thoctar");
        harness.setHand(player1, List.of(new AgonyWarp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, List.of(creature.getId(), creature.getId()));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void triggersForItsOwnEntryWhenItEntersAsFivePowerCreature() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.castFromHand(player1, new MightyEmergence(), "{2}{G}");
        harness.passBothPriorities();
        Permanent emergence = findPermanent(player1, "Mighty Emergence");
        assertThat(gqs.getEffectivePower(gd, emergence)).isEqualTo(5);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(emergence.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private Permanent findAvatar(Player player) {
        return findPermanent(player, "Avatar of Might");
    }
}
