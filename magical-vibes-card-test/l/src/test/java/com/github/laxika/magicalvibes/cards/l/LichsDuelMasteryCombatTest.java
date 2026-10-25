package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.Earthquake;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GravebaneZombie;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.k.KillSuitCultist;
import com.github.laxika.magicalvibes.cards.u.UnderdarkBeholder;
import com.github.laxika.magicalvibes.cards.w.WheelOfSunAndMoon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FailOnLegacyDepartureFallback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lich's Duel Mastery's sacrifice asked together with other departures caused by the same damage: each of player 2's
 * noncreature, shieldless Masteries sits between Wheel of Sun and Moon and the opponent's Leyline of the Void.
 */
@CardUsed({LichsDuelMastery.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class, KalitasTraitorOfGhet.class,
        GrizzlyBears.class})
class LichsDuelMasteryCombatTest extends BaseCardTest {

    @Test
    @CardUsed(UnderdarkBeholder.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("The defending player's Mastery and a blocker's \"sacrifice it instead\" are both asked before any combat damage")
    void masteryAskedBeforeTheBlockersSacrificeInsteadOfCombatDamage() {
        assertMasteryAndBeholderAskedFirst(true);
    }

    @Test
    @CardUsed(UnderdarkBeholder.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Declaring the blocked attacker first asks the Mastery and the blocker's \"sacrifice it instead\" the same way")
    void blockedAttackerDeclaredFirstAsksTheMasteryAndTheBlockerFirst() {
        assertMasteryAndBeholderAskedFirst(false);
    }

    @Test
    @CardUsed({KillSuitCultist.class, GiantSpider.class, GravebaneZombie.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("A blocker's \"destroy it instead\" and the defending player's Mastery are both asked before any life is lost")
    void blockerDestructionAndMasterySacrificeAreBothAskedBeforeAnyLifeIsLost() {
        masteryBetweenWheelAndLeyline(player2, player1);
        Permanent cultist = addCreatureReady(player1, new KillSuitCultist());
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent zombie = addCreatureReady(player2, new GravebaneZombie());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cultist), null,
                zombie.getId());
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        declareAttackersAndPrepareBlockers(List.of(battlefield.indexOf(spider), battlefield.indexOf(bears)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(zombie), battlefield.indexOf(spider))));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .contains("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        harness.assertLife(player2, 20);
        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Exile it");
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player2, 20);
        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getId()).isEqualTo(zombie.getCard().getId());
        harness.assertLife(player2, 20);
        assertThat(spider.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(Earthquake.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Earthquake asks both players' Masteries before either life total changes")
    void earthquakeAsksBothMasteriesBeforeAnyLifeChanges() {
        masteryBetweenWheelAndLeyline(player1, player2);
        masteryBetweenWheelAndLeyline(player2, player1);
        harness.setHand(player1, List.of(new Earthquake()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        for (Player asked : List.of(player1, player2)) {
            var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
            assertThat(choice.playerId()).isEqualTo(asked.getId());
            harness.assertLife(player1, 20);
            harness.assertLife(player2, 20);
            harness.handleListChoice(asked, "Exile it");
        }

        harness.assertNotOnBattlefield(player1, "Lich's Duel Mastery");
        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void assertMasteryAndBeholderAskedFirst(boolean unblockedAttackerFirst) {
        Permanent mastery = masteryBetweenWheelAndLeyline(player2, player1);
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent unblocked = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocked = addCreatureReady(player1, new GrizzlyBears());
        Permanent beholder = harness.enterBattlefieldAndReturn(player2, new UnderdarkBeholder());
        beholder.setCounterCount(CounterType.EYEBALL, 1);
        beholder.setSummoningSick(false);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        declareAttackersAndPrepareBlockers(unblockedAttackerFirst
                ? List.of(battlefield.indexOf(unblocked), battlefield.indexOf(blocked))
                : List.of(battlefield.indexOf(blocked), battlefield.indexOf(unblocked)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(beholder), battlefield.indexOf(blocked))));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Exile it");
        harness.assertOnBattlefield(player2, "Underdark Beholder");
        harness.assertLife(player2, 20);
        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Exile it",
                        "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertOnBattlefield(player2, "Underdark Beholder");
        assertThat(beholder.getCounterCount(CounterType.EYEBALL)).isEqualTo(1);
        harness.assertLife(player2, 20);
        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertNotOnBattlefield(player2, "Underdark Beholder");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(beholder.getCard());
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent masteryBetweenWheelAndLeyline(Player owner, Player opponent) {
        harness.setLife(owner, 20);
        Permanent mastery = harness.addToBattlefieldAndReturn(owner, new LichsDuelMastery());
        harness.addToBattlefieldAndReturn(owner, new WheelOfSunAndMoon()).setAttachedTo(owner.getId());
        harness.addToBattlefield(opponent, new LeylineOfTheVoid());
        return mastery;
    }
}
