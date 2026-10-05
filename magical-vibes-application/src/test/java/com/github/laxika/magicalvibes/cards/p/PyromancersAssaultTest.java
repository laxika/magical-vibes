package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.z.ZadasCommando;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PyromancersAssault.class, LightningBolt.class, ZadasCommando.class})
class PyromancersAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a chosen player when you cast your second spell each turn")
    void dealsDamageOnSecondSpell() {
        harness.addToBattlefield(player1, new PyromancersAssault());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    void countsAssaultItselfAsFirstSpellAndResolvesTriggerBeforeSecondSpell() {
        harness.setHand(player1, List.of(new PyromancersAssault(), new ZadasCommando()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Zada's Commando");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Zada's Commando");
    }

    @Test
    void doesNotTriggerWhenAssaultIsSecondSpellOrForThirdSpell() {
        harness.setHand(player1, List.of(new ZadasCommando(), new PyromancersAssault(), new ZadasCommando()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    void canDamageCreatureAndDoesNotTriggerForThirdSpell() {
        harness.addToBattlefield(player1, new PyromancersAssault());
        harness.addToBattlefield(player2, new ZadasCommando());
        var targetId = harness.getPermanentId(player2, "Zada's Commando");
        harness.setHand(player1, List.of(new ZadasCommando(), new ZadasCommando(), new ZadasCommando()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Zada's Commando");
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
    }

    @Test
    void doesNotTriggerForOpponentsSpellsOrCountThemAsControllersSpells() {
        harness.addToBattlefield(player1, new PyromancersAssault());
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(gd.stack).isEmpty();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    void triggersAgainOnOpponentsTurnAfterSpellCountResets() {
        harness.addToBattlefield(player1, new PyromancersAssault());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);

        harness.setLibrary(player2, List.of(new PyromancersAssault()));
        harness.passUntil(TurnStep.UPKEEP);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(4);
    }
}
