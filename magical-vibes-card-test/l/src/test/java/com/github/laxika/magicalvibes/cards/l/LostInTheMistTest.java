package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.s.SilverchaseFox;
import com.github.laxika.magicalvibes.cards.o.OneEyedScarecrow;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.r.RangersGuile;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LostInTheMist.class, SilverchaseFox.class, OneEyedScarecrow.class, RangersGuile.class})
class LostInTheMistTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting both a spell and a permanent")
    void castingPutsOnStackWithBothTargets() {
        SilverchaseFox spell = new SilverchaseFox();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 2);

        OneEyedScarecrow permanent = new OneEyedScarecrow();
        UUID permanentId = harness.addToBattlefieldAndReturn(player1, permanent).getId();

        harness.setHand(player2, List.of(new LostInTheMist()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId(), permanentId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry lostEntry = gd.stack.getLast();
        assertThat(lostEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(lostEntry.getCard()).isInstanceOf(LostInTheMist.class);
        assertThat(lostEntry.getTargetId()).isEqualTo(spell.getId());
        assertThat(lostEntry.getTargetIds()).containsExactly(permanentId);
    }

    @Test
    @DisplayName("Resolving counters the spell and bounces the permanent")
    void countersSpellAndBouncesPermanent() {
        SilverchaseFox spell = new SilverchaseFox();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 2);

        OneEyedScarecrow permanent = new OneEyedScarecrow();
        UUID permanentId = harness.addToBattlefieldAndReturn(player1, permanent).getId();

        harness.setHand(player2, List.of(new LostInTheMist()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId(), permanentId);
        harness.passBothPriorities();

        // Spell was countered
        harness.assertInGraveyard(player1, "Silverchase Fox");
        harness.assertNotOnBattlefield(player1, "Silverchase Fox");

        // Permanent was bounced
        harness.assertNotOnBattlefield(player1, "One-Eyed Scarecrow");
        harness.assertInHand(player1, "One-Eyed Scarecrow");
    }

    @Test
    @DisplayName("Can bounce own permanent while countering opponent's spell")
    void canBounceOwnPermanent() {
        SilverchaseFox spell = new SilverchaseFox();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 2);

        OneEyedScarecrow permanent = new OneEyedScarecrow();
        UUID permanentId = harness.addToBattlefieldAndReturn(player2, permanent).getId();

        harness.setHand(player2, List.of(new LostInTheMist()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId(), permanentId);
        harness.passBothPriorities();

        // Spell was countered
        harness.assertInGraveyard(player1, "Silverchase Fox");

        // Own permanent was bounced
        harness.assertNotOnBattlefield(player2, "One-Eyed Scarecrow");
        harness.assertInHand(player2, "One-Eyed Scarecrow");
    }

    @Test
    @DisplayName("Lost in the Mist goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        SilverchaseFox spell = new SilverchaseFox();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 2);

        SilverchaseFox target = new SilverchaseFox();
        UUID targetId = harness.addToBattlefieldAndReturn(player1, target).getId();

        harness.setHand(player2, List.of(new LostInTheMist()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId(), targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Lost in the Mist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Still bounces permanent if spell target is no longer on the stack")
    void stillBouncesIfSpellTargetGone() {
        SilverchaseFox spell = new SilverchaseFox();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 2);

        OneEyedScarecrow permanent = new OneEyedScarecrow();
        UUID permanentId = harness.addToBattlefieldAndReturn(player1, permanent).getId();

        harness.setHand(player2, List.of(new LostInTheMist()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId(), permanentId);

        // Remove the spell target from the stack before resolution
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Silverchase Fox"));

        harness.passBothPriorities();

        // Spell does NOT fizzle — permanent target is still legal
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(log -> log.contains("fizzles"));

        // Permanent was still bounced
        harness.assertNotOnBattlefield(player1, "One-Eyed Scarecrow");
        harness.assertInHand(player1, "One-Eyed Scarecrow");
    }

    @Test
    @DisplayName("Still counters spell if permanent target is no longer on the battlefield")
    void stillCountersIfPermanentTargetGone() {
        SilverchaseFox spell = new SilverchaseFox();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 2);

        OneEyedScarecrow permanent = new OneEyedScarecrow();
        UUID permanentId = harness.addToBattlefieldAndReturn(player1, permanent).getId();

        harness.setHand(player2, List.of(new LostInTheMist()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId(), permanentId);

        // Remove the permanent target from the battlefield before resolution
        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Spell does NOT fizzle — spell target is still legal
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(log -> log.contains("fizzles"));

        // Spell was still countered
        harness.assertInGraveyard(player1, "Silverchase Fox");
        harness.assertNotOnBattlefield(player1, "Silverchase Fox");
    }

    @Test
    @DisplayName("Fizzles if both targets are illegal")
    void fizzlesIfBothTargetsIllegal() {
        SilverchaseFox spell = new SilverchaseFox();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 2);

        OneEyedScarecrow permanent = new OneEyedScarecrow();
        UUID permanentId = harness.addToBattlefieldAndReturn(player1, permanent).getId();

        harness.setHand(player2, List.of(new LostInTheMist()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId(), permanentId);

        // Remove both targets before resolution
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Silverchase Fox"));
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Entire spell fizzles
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));

        // Lost in the Mist goes to graveyard
        harness.assertInGraveyard(player2, "Lost in the Mist");
    }

    @Test
    @DisplayName("Counters the spell but does not bounce a creature that gains hexproof")
    void doesNotBounceTargetThatGainsHexproof() {
        SilverchaseFox spell = new SilverchaseFox();
        harness.setHand(player1, List.of(spell, new RangersGuile()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        UUID permanentId = harness.addToBattlefieldAndReturn(player1, new OneEyedScarecrow()).getId();
        harness.setHand(player2, List.of(new LostInTheMist()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId(), permanentId);
        harness.castAndResolveInstant(player1, 0, permanentId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Silverchase Fox");
        harness.assertOnBattlefield(player1, "One-Eyed Scarecrow");
        harness.assertNotInHand(player1, "One-Eyed Scarecrow");
        harness.assertInGraveyard(player2, "Lost in the Mist");
    }

    @Test
    @DisplayName("Returns a stolen permanent to its owner rather than its controller")
    void returnsStolenPermanentToOwner() {
        SilverchaseFox spell = new SilverchaseFox();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 2);
        UUID permanentId = harness.addToBattlefieldAndReturn(player2, new OneEyedScarecrow()).getId();
        harness.getGameData().stolenCreatures.put(permanentId, player1.getId());
        harness.setHand(player2, List.of(new LostInTheMist()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId(), permanentId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Silverchase Fox");
        harness.assertNotOnBattlefield(player2, "One-Eyed Scarecrow");
        harness.assertInHand(player1, "One-Eyed Scarecrow");
        harness.assertNotInHand(player2, "One-Eyed Scarecrow");
    }

    @Test
    @DisplayName("Still bounces the permanent when the target spell cannot be countered")
    void bouncesPermanentWhenSpellCannotBeCountered() {
        SilverchaseFox spell = new SilverchaseFox();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 2);
        UUID permanentId = harness.addToBattlefieldAndReturn(player1, new OneEyedScarecrow()).getId();
        harness.setHand(player2, List.of(new LostInTheMist()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.getGameData().spellsMadeUncounterable.add(spell.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId(), permanentId);
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).hasSize(1);
        harness.assertNotInGraveyard(player1, "Silverchase Fox");
        harness.assertInHand(player1, "One-Eyed Scarecrow");
        harness.assertNotOnBattlefield(player1, "One-Eyed Scarecrow");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Silverchase Fox");
    }
}
