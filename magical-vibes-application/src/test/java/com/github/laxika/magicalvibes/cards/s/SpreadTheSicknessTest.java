package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BlightsteelColossus;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({SpreadTheSickness.class, GrizzlyBears.class, Spellbook.class, BlightsteelColossus.class})
class SpreadTheSicknessTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature and then proliferates")
    void destroysCreatureAndProliferates() {
        // Set up: two creatures, one with a -1/-1 counter
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        Permanent otherBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        otherBears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SpreadTheSickness()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, targetId);

        // Target creature destroyed
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // Proliferate choice is now awaited — choose the creature with counters
        harness.handleMultiplePermanentsChosen(player1, List.of(otherBears.getId()));

        assertThat(otherBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Destroys creature and proliferates choosing none")
    void destroysCreatureAndProliferatesNone() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SpreadTheSickness()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        // Bears was destroyed — proliferate has no eligible permanents now
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Fizzles when target creature is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        Permanent otherBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        otherBears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SpreadTheSickness()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, targetId);

        // Remove the target before resolution
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(targetId));

        harness.passBothPriorities();

        // Spell fizzles — no proliferate either
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(otherBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target non-creature permanents")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Spellbook());
        UUID spellbookId = harness.getPermanentId(player2, "Spellbook");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SpreadTheSickness()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, spellbookId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SpreadTheSickness()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Spread the Sickness");
    }

    @Test
    @DisplayName("Proliferates all counter kinds on an indestructible target that survives")
    void proliferatesSurvivingIndestructibleTarget() {
        Permanent colossus = harness.addToBattlefieldAndReturn(player2, new BlightsteelColossus());
        colossus.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        colossus.setCounterCount(CounterType.STUN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SpreadTheSickness()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, colossus.getId());
        harness.assertOnBattlefield(player2, "Blightsteel Colossus");
        harness.handleMultiplePermanentsChosen(player1, List.of(colossus.getId()));

        assertThat(colossus.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(colossus.getCounterCount(CounterType.STUN)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Spread the Sickness");
    }

    @Test
    @DisplayName("Can proliferate a player while leaving another player's counters unchanged")
    void proliferatesOnlyChosenPlayer() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SpreadTheSickness()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        harness.assertInGraveyard(player1, "Spread the Sickness");
    }

    @Test
    @DisplayName("Can decline proliferating even when counters are available")
    void declinesAvailableCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        survivor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SpreadTheSickness()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Spread the Sickness");
    }
}
