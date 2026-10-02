package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Persuasion;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbigalePoetLaureateHeroicStanza.class, GrizzlyBears.class, Shock.class, Persuasion.class})
class AbigalePoetLaureateHeroicStanzaTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature spell prepares Abigale and exiles a castable Heroic Stanza copy")
    void castingCreaturePreparesAbigale() {
        Permanent abigale = addCreatureReady(player1, new AbigalePoetLaureateHeroicStanza());

        castATriggeringCreature(player1);

        assertThat(abigale.isPrepared()).isTrue();
        UUID copyId = abigale.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        // The prepare-spell copy sits in exile with a play permission for the controller.
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
        // Not flagged to expire at end of turn — it persists until cast or Abigale leaves.
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(copyId);
    }

    @Test
    @DisplayName("Casting a non-creature spell does not prepare Abigale")
    void castingNoncreatureDoesNotPrepare() {
        Permanent abigale = addCreatureReady(player1, new AbigalePoetLaureateHeroicStanza());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(abigale.isPrepared()).isFalse();
        assertThat(abigale.getPreparedSpellCardId()).isNull();
    }

    @Test
    @DisplayName("An already-prepared Abigale does not prepare again on a second creature cast")
    void doesNotPrepareTwice() {
        Permanent abigale = addCreatureReady(player1, new AbigalePoetLaureateHeroicStanza());

        castATriggeringCreature(player1);
        UUID firstCopyId = abigale.getPreparedSpellCardId();
        assertThat(firstCopyId).isNotNull();
        harness.passBothPriorities(); // resolve the creature spell itself

        // Cast a second creature while still prepared.
        castATriggeringCreature(player1);

        assertThat(abigale.isPrepared()).isTrue();
        assertThat(abigale.getPreparedSpellCardId()).isEqualTo(firstCopyId);
        // Only one Heroic Stanza copy ever exists in exile at a time.
        assertThat(countExiledHeroicStanzas()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting the prepared Heroic Stanza copy unprepares Abigale and adds a +1/+1 counter")
    void castingPrepareCopyUnpreparesAndCounters() {
        Permanent abigale = addCreatureReady(player1, new AbigalePoetLaureateHeroicStanza());

        castATriggeringCreature(player1);
        harness.passBothPriorities(); // resolve the creature spell
        UUID copyId = abigale.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2); // {1}{W/B}
        harness.castFromExile(player1, copyId, abigale.getId());
        harness.passBothPriorities(); // resolve Heroic Stanza

        assertThat(abigale.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(abigale.isPrepared()).isFalse();
        assertThat(abigale.getPreparedSpellCardId()).isNull();
        // The copy has left exile and its permission is gone.
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    @Test
    @DisplayName("After casting the copy, Abigale can be prepared again by another creature cast")
    void canPrepareAgainAfterCasting() {
        Permanent abigale = addCreatureReady(player1, new AbigalePoetLaureateHeroicStanza());

        castATriggeringCreature(player1);
        harness.passBothPriorities();
        UUID firstCopyId = abigale.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, firstCopyId, abigale.getId());
        harness.passBothPriorities();
        assertThat(abigale.isPrepared()).isFalse();

        // A new creature cast prepares her again with a fresh copy.
        castATriggeringCreature(player1);
        assertThat(abigale.isPrepared()).isTrue();
        assertThat(abigale.getPreparedSpellCardId()).isNotNull();
        assertThat(abigale.getPreparedSpellCardId()).isNotEqualTo(firstCopyId);
    }

    @Test
    @DisplayName("When prepared Abigale leaves the battlefield, the exiled copy ceases to exist")
    void leavingBattlefieldRemovesExiledCopy() {
        Permanent abigale = addCreatureReady(player1, new AbigalePoetLaureateHeroicStanza());

        castATriggeringCreature(player1);
        UUID copyId = abigale.getPreparedSpellCardId();
        assertThat(gd.findExiledCard(copyId)).isNotNull();

        // Lethal damage destroys Abigale (toughness 3) via state-based actions.
        abigale.setMarkedDamage(3);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(abigale);
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    @Test
    @DisplayName("An opponent's creature spell does not prepare Abigale")
    void opponentsCreatureDoesNotPrepareAbigale() {
        Permanent abigale = addCreatureReady(player1, new AbigalePoetLaureateHeroicStanza());

        castATriggeringCreature(player2);

        assertThat(abigale.isPrepared()).isFalse();
        assertThat(abigale.getPreparedSpellCardId()).isNull();
    }

    @Test
    @DisplayName("A creature entering without being cast does not prepare Abigale")
    void creatureEnteringDoesNotPrepareAbigale() {
        Permanent abigale = addCreatureReady(player1, new AbigalePoetLaureateHeroicStanza());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(abigale.isPrepared()).isFalse();
        assertThat(abigale.getPreparedSpellCardId()).isNull();
    }

    @Test
    @DisplayName("Heroic Stanza accepts black mana and an opposing creature, and unprepares on casting")
    void blackManaCanCastStanzaOnOpposingCreature() {
        Permanent abigale = addCreatureReady(player1, new AbigalePoetLaureateHeroicStanza());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castATriggeringCreature(player1);
        harness.passBothPriorities();
        UUID copyId = abigale.getPreparedSpellCardId();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromExile(player1, copyId, target.getId());

        assertThat(abigale.isPrepared()).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Abigale remains unprepared when Heroic Stanza's target dies before resolution")
    void losingStanzasTargetDoesNotRestorePreparedState() {
        Permanent abigale = addCreatureReady(player1, new AbigalePoetLaureateHeroicStanza());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castATriggeringCreature(player1);
        harness.passBothPriorities();
        UUID copyId = abigale.getPreparedSpellCardId();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, copyId, target.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(abigale.isPrepared()).isFalse();
        assertThat(abigale.getPreparedSpellCardId()).isNull();
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(abigale.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The new controller of prepared Abigale can cast her Heroic Stanza copy")
    void newControllerCanCastPreparedSpell() {
        Permanent abigale = addCreatureReady(player1, new AbigalePoetLaureateHeroicStanza());
        castATriggeringCreature(player1);
        harness.passBothPriorities();
        UUID copyId = abigale.getPreparedSpellCardId();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Persuasion()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castEnchantment(player2, 0, abigale.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(abigale);
        assertThat(abigale.isPrepared()).isTrue();

        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castFromExile(player2, copyId, abigale.getId());
        harness.passBothPriorities();

        assertThat(abigale.isPrepared()).isFalse();
        assertThat(abigale.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    /** Casts a Grizzly Bears from hand as the given player and resolves the prepare trigger (top of stack). */
    private void castATriggeringCreature(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities(); // resolve the BecomePrepared trigger sitting on top of the stack
    }

    private long countExiledHeroicStanzas() {
        return gd.exiledCards.stream()
                .filter(e -> "Heroic Stanza".equals(e.card().getName()))
                .count();
    }
}
