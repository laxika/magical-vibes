package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.t.TezzeretAgentOfBolas;

import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
import com.github.laxika.magicalvibes.cards.g.GustSkimmer;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlisterstickShaman.class, LeoninSkyhunter.class, GustSkimmer.class, TezzeretAgentOfBolas.class})
class BlisterstickShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Blisterstick Shaman does not target a creature")
    void castingDoesNotTargetCreature() {
        harness.addToBattlefield(player2, new LeoninSkyhunter());
        harness.castFromHand(player1, new BlisterstickShaman(), "{2}{R}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Blisterstick Shaman");
        assertThat(entry.getTargetId()).isNull();
    }

    @Test
    @DisplayName("Casting Blisterstick Shaman does not target a player")
    void castingDoesNotTargetPlayer() {
        harness.castFromHand(player1, new BlisterstickShaman(), "{2}{R}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Blisterstick Shaman");
        assertThat(entry.getTargetId()).isNull();
    }

    @Test
    @DisplayName("Resolving Blisterstick Shaman enters battlefield and triggers ETB")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.addToBattlefield(player2, new LeoninSkyhunter());
        UUID targetId = harness.getPermanentId(player2, "Leonin Skyhunter");
        harness.castFromHand(player1, new BlisterstickShaman(), "{2}{R}");

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Blisterstick Shaman");

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Blisterstick Shaman");
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB deals 1 damage to target creature, killing a creature with 1 toughness")
    void etbDeals1DamageKillsOneToughnessCreature() {
        GustSkimmer smallCreature = new GustSkimmer();
        harness.addToBattlefield(player2, smallCreature);
        UUID targetId = harness.getPermanentId(player2, "Gust-Skimmer");
        harness.castFromHand(player1, new BlisterstickShaman(), "{2}{R}");

        // Resolve creature spell
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Gust-Skimmer");
        harness.assertInGraveyard(player2, "Gust-Skimmer");
    }

    @Test
    @DisplayName("ETB deals 1 damage to a 2/2 creature but does not kill it")
    void etbDeals1DamageDoesNotKillTwoTwo() {
        harness.addToBattlefield(player2, new LeoninSkyhunter());
        UUID targetId = harness.getPermanentId(player2, "Leonin Skyhunter");
        harness.castFromHand(player1, new BlisterstickShaman(), "{2}{R}");

        // Resolve creature spell
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Leonin Skyhunter");
    }

    @Test
    @DisplayName("ETB deals 1 damage to target player")
    void etbDeals1DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new BlisterstickShaman(), "{2}{R}");

        // Resolve creature spell
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Can cast without choosing an ETB target")
    void canCastWithoutTarget() {
        harness.castFromHand(player1, new BlisterstickShaman(), "{2}{R}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Blisterstick Shaman");
    }

    @Test
    @DisplayName("ETB requires a target even when none was supplied during casting")
    void etbTriggersWithoutCastTimeTarget() {
        harness.castFromHand(player1, new BlisterstickShaman(), "{2}{R}");

        // Resolve creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Blisterstick Shaman");
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new LeoninSkyhunter());
        UUID targetId = harness.getPermanentId(player2, "Leonin Skyhunter");
        harness.castFromHand(player1, new BlisterstickShaman(), "{2}{R}");

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);

        // Remove target before ETB resolves
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("The creature spell is untargeted and its ETB target is chosen on entry")
    void creatureSpellIsUntargetedAndEtbChoosesTargetOnEntry() {
        harness.setHand(player1, List.of(new BlisterstickShaman()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack.getFirst().getTargetId()).isNull();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB can target the Shaman itself")
    void etbCanTargetItself() {
        harness.castFromHand(player1, new BlisterstickShaman(), "{2}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Blisterstick Shaman"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Blisterstick Shaman");
        harness.assertInGraveyard(player1, "Blisterstick Shaman");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB still deals damage after the Shaman leaves the battlefield")
    void etbResolvesAfterSourceLeaves() {
        harness.castFromHand(player1, new BlisterstickShaman(), "{2}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("ETB can damage a creature controlled by its controller")
    void etbCanTargetOwnCreature() {
        harness.addToBattlefield(player1, new GustSkimmer());
        harness.castFromHand(player1, new BlisterstickShaman(), "{2}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Gust-Skimmer"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gust-Skimmer");
        harness.assertOnBattlefield(player1, "Blisterstick Shaman");
    }

    @Test
    @DisplayName("ETB deals 1 damage to a planeswalker")
    void etbDamagesPlaneswalker() {
        Permanent tezzeret = new Permanent(new TezzeretAgentOfBolas());
        tezzeret.setCounterCount(CounterType.LOYALTY, 3);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(tezzeret);
        harness.castFromHand(player1, new BlisterstickShaman(), "{2}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, tezzeret.getId());
        harness.passBothPriorities();

        assertThat(tezzeret.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Tezzeret, Agent of Bolas");
        harness.assertLife(player2, 20);
    }
}
