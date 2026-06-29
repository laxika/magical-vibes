package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AbigalePoetLaureateHeroicStanzaTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature spell prepares Abigale and exiles a castable Heroic Stanza copy")
    void castingCreaturePreparesAbigale() {
        Permanent abigale = addReady(player1, new AbigalePoetLaureateHeroicStanza());

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
        Permanent abigale = addReady(player1, new AbigalePoetLaureateHeroicStanza());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(abigale.isPrepared()).isFalse();
        assertThat(abigale.getPreparedSpellCardId()).isNull();
    }

    @Test
    @DisplayName("An already-prepared Abigale does not prepare again on a second creature cast")
    void doesNotPrepareTwice() {
        Permanent abigale = addReady(player1, new AbigalePoetLaureateHeroicStanza());

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
        Permanent abigale = addReady(player1, new AbigalePoetLaureateHeroicStanza());

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
        Permanent abigale = addReady(player1, new AbigalePoetLaureateHeroicStanza());

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
        Permanent abigale = addReady(player1, new AbigalePoetLaureateHeroicStanza());

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

    // ===== Helpers =====

    /** Casts a Grizzly Bears from hand as the given player and resolves the prepare trigger (top of stack). */
    private void castATriggeringCreature(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.setHand(player, List.of(new GrizzlyBears()));
        harness.castCreature(player, 0);
        harness.passBothPriorities(); // resolve the BecomePrepared trigger sitting on top of the stack
    }

    private long countExiledHeroicStanzas() {
        return gd.exiledCards.stream()
                .filter(e -> "Heroic Stanza".equals(e.card().getName()))
                .count();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
