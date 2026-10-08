package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GuildGlobe;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WanderersStrike.class, PrimordialWurm.class, GuildGlobe.class})
class WanderersStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target creature and then proliferates")
    void exilesCreatureAndProliferates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        UUID targetId = target.getId();
        UUID targetCardId = target.getCard().getId();

        Permanent otherWurm = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        otherWurm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WanderersStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Primordial Wurm");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(targetCardId));

        harness.handleMultiplePermanentsChosen(player1, List.of(otherWurm.getId()));

        assertThat(otherWurm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Fizzles if target creature leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new PrimordialWurm());
        UUID targetId = harness.getPermanentId(player2, "Primordial Wurm");

        Permanent otherWurm = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        otherWurm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WanderersStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(targetId));
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(otherWurm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GuildGlobe());
        UUID targetId = harness.getPermanentId(player2, "Guild Globe");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WanderersStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can exile its controller's creature with no counters to proliferate")
    void exilesOwnCreatureWithoutCounterRecipients() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WanderersStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Primordial Wurm");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        harness.assertInGraveyard(player1, "Wanderer's Strike");
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("May choose no recipients when proliferating")
    void mayDeclineAllCounterRecipients() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        recipient.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WanderersStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Primordial Wurm");
        harness.assertInGraveyard(player1, "Wanderer's Strike");
    }

    @Test
    @DisplayName("Proliferates each existing counter kind on selected permanents and players")
    void proliferatesAllKindsOnSelectedRecipientsOnly() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        Permanent selected = harness.addToBattlefieldAndReturn(player2, new GuildGlobe());
        selected.setCounterCount(CounterType.CHARGE, 2);
        selected.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent unselected = harness.addToBattlefieldAndReturn(player1, new GuildGlobe());
        unselected.setCounterCount(CounterType.CHARGE, 4);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 3);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WanderersStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(selected.getId(), player2.getId()));

        assertThat(selected.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(selected.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(unselected.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
        harness.assertInGraveyard(player1, "Wanderer's Strike");
    }
}
