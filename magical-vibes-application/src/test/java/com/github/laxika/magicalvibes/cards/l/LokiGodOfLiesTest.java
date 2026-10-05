package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ConeOfFlame;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SeedsOfStrength;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LokiGodOfLies.class, GiantGrowth.class, GrizzlyBears.class, Shock.class,
        ConeOfFlame.class, SeedsOfStrength.class, Unsummon.class, HolyStrength.class})
class LokiGodOfLiesTest extends BaseCardTest {

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Gains control of a single-targeted creature, untaps it and grants haste on its turn")
    void gainsControlUntapsAndGrantsHasteOnItsTurn() {
        harness.addToBattlefield(player1, new LokiGodOfLies());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        prepareMainPhase(player1);
        harness.setHand(player1, java.util.List.of(new GiantGrowth()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities(); // resolve Loki's trigger

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        resolveAllTriggers();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not untap or grant haste during an opponent's turn")
    void doesNotUntapOrGrantHasteDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new LokiGodOfLies());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        prepareMainPhase(player2);
        harness.setHand(player1, java.util.List.of(new GiantGrowth()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities(); // resolve Loki's trigger

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger for a spell targeting a player")
    void doesNotTriggerForNonCreatureTarget() {
        harness.addToBattlefield(player1, new LokiGodOfLies());
        prepareMainPhase(player1);
        harness.setHand(player1, java.util.List.of(new Shock()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void untapsAndGrantsHasteToCreatureAlreadyControlled() {
        harness.addToBattlefield(player1, new LokiGodOfLies());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.tap();
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void doesNotTriggerForOpponentsSpell() {
        harness.addToBattlefield(player1, new LokiGodOfLies());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void doesNotTriggerForUntargetedCreatureSpell() {
        harness.addToBattlefield(player1, new LokiGodOfLies());
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotTriggerForCreatureAndPlayerTargets() {
        harness.addToBattlefield(player1, new LokiGodOfLies());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, List.of(target.getId(), player1.getId(), player2.getId()));

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void triggersOnceWhenAllTargetOccurrencesAreSameCreature() {
        harness.addToBattlefield(player1, new LokiGodOfLies());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new SeedsOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, List.of(target.getId(), target.getId(), target.getId()));

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotTriggerWhenSpellTargetsDifferentCreatures() {
        harness.addToBattlefield(player1, new LokiGodOfLies());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new SeedsOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, List.of(first.getId(), first.getId(), second.getId()));

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void triggerStillResolvesAfterLokiLeavesBattlefield() {
        Permanent loki = harness.addToBattlefieldAndReturn(player1, new LokiGodOfLies());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, loki.getId());
        harness.assertInHand(player1, "Loki, God of Lies");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void triggerDoesNotAffectCreatureThatLeftBattlefield() {
        harness.addToBattlefield(player1, new LokiGodOfLies());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void auraSpellTriggersControlChangeBeforeAuraResolves() {
        harness.addToBattlefield(player1, new LokiGodOfLies());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.assertNotOnBattlefield(player1, "Holy Strength");
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Holy Strength").getAttachedTo()).isEqualTo(target.getId());
    }
}
