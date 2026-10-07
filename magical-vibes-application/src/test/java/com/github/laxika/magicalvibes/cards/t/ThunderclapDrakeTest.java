package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderclapDrake.class, Divination.class, LightningBolt.class, Negate.class, TalrandSkySummoner.class})
class ThunderclapDrakeTest extends BaseCardTest {

    @Test
    void reducesInstantAndSorcerySpellsByOneGenericMana() {
        addReadyDrake();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Divination"));
    }

    @Test
    void doesNotCopyWhenNoCommanderHasBeenCast() {
        addReadyDrake();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.pendingNextInstantSorceryCopyThisTurnDynamicCounts)
                .doesNotContainKey(player1.getId());
        assertThat(gd.stack).noneMatch(entry -> entry.isCopy());
    }

    @Test
    void reducesInstantSpellsByOneGenericMana() {
        addReadyDrake();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        var boltId = gd.stack.getLast().getTargetableId();
        harness.setHand(player1, List.of(new Negate()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, boltId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Lightning Bolt");
    }

    @Test
    void copiesTheNextSpellForEachCommanderCast() {
        prepareCommanderGame();
        Card commander = addCommanderToCommandZone();
        castCommander(commander, 2);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        castCommander(commander, 4);

        addReadyDrake();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);
    }

    @Test
    void createsOneDelayedTriggerThatMakesAllCopies() {
        prepareCommanderGame();
        Card commander = addCommanderToCommandZone();
        castCommander(commander, 2);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        castCommander(commander, 4);
        activateDrakeAndResolve();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);
    }

    @Test
    void triggersEvenWhenCommanderCastCountIsZero() {
        activateDrakeAndResolve();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(1);
    }

    @Test
    void countsCommanderCastsWhenTheDelayedTriggerResolves() {
        prepareCommanderGame();
        Card commander = addCommanderToCommandZone();
        castCommander(commander, 2);
        activateDrakeAndResolve();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);

        gd.recordCommanderCastFromCommandZone(player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);
    }

    @Test
    void sacrificeIsPaidBeforeTheAbilityResolves() {
        addReadyDrake();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Thunderclap Drake");
        harness.assertInGraveyard(player1, "Thunderclap Drake");
        assertThat(gd.stack).hasSize(1);
    }

    private void activateDrakeAndResolve() {
        addReadyDrake();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addReadyDrake() {
        return addCreatureReady(player1, new ThunderclapDrake());
    }

    private void prepareCommanderGame() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.format = DeckFormat.COMMANDER;
    }

    private Card addCommanderToCommandZone() {
        Card commander = new TalrandSkySummoner();
        commander.setOwnerId(player1.getId());
        commander.freeze();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        return commander;
    }

    private void castCommander(Card commander, int mana) {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, mana);
        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));
        gd.stack.clear();
        gd.priorityPassedBy.clear();
    }
}
