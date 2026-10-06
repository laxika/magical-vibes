package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrizzledLeotau;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShieldOfTheRighteous.class, GrizzlyBears.class, GrizzledLeotau.class})
class ShieldOfTheRighteousTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +0/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears()); // Grizzly Bears 2/2
        Permanent shield = addShield(player1);
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equipped creature has vigilance")
    void equippedCreatureHasVigilance() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShield(player1);
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Unequipped creature gets no boost or vigilance")
    void unequippedCreatureNoBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addShield(player1); // not attached

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("When equipped creature blocks, a trigger auto-targeting the blocked attacker is created")
    void blockingCreatesTrigger() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent shield = addShield(player2);
        shield.setAttachedTo(blocker.getId());

        Permanent attacker = addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Shield of the Righteous")
                        && se.isNonTargeting()
                        && se.getTargetId().equals(attacker.getId())
                        && se.getSourcePermanentId().equals(shield.getId()));
    }

    @Test
    @DisplayName("Resolving the block trigger sets skipUntapCount on the blocked attacker")
    void resolvingSetsSkipUntapCount() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent shield = addShield(player2);
        shield.setAttachedTo(blocker.getId());

        Permanent attacker = addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("No trigger when the Shield is not attached to any creature")
    void noTriggerWhenNotEquipped() {
        addCreatureReady(player2, new GrizzlyBears());
        addShield(player2); // on battlefield but unattached

        addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack)
                .noneMatch(se -> se.getCard().getName().equals("Shield of the Righteous"));
    }

    @Test
    @DisplayName("Equip costs two mana and moving the Shield transfers its bonuses")
    void equipTransfersBonuses() {
        Permanent shield = addShield(player1);
        Permanent first = addCreatureReady(player1, new GrizzledLeotau());
        Permanent second = addCreatureReady(player1, new GrizzledLeotau());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature attacks without tapping and does not create the block trigger")
    void attackingWithVigilanceDoesNotTriggerShield() {
        Permanent attacker = addCreatureReady(player1, new GrizzledLeotau());
        Permanent shield = addShield(player1);
        shield.setAttachedTo(attacker.getId());
        addCreatureReady(player2, new GrizzledLeotau());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(attacker.isAttacking()).isTrue();
        assertThat(attacker.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Block restriction survives Shield removal and expires after the attacker's next untap step")
    void blockRestrictionSurvivesSourceRemovalAndExpires() {
        Permanent blocker = addCreatureReady(player2, new GrizzledLeotau());
        Permanent shield = addShield(player2);
        shield.setAttachedTo(blocker.getId());
        Permanent attacker = addCreatureReady(player1, new GrizzledLeotau());
        attacker.setAttacking(true);
        attacker.tap();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        gd.playerBattlefields.get(player2.getId()).remove(shield);
        resolveAllTriggers();

        harness.performUntapStep(player2);
        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isFalse();
    }

    private Permanent addShield(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ShieldOfTheRighteous());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyAttacker(Player player) {
        Permanent perm = addCreatureReady(player, new GrizzlyBears());
        perm.setAttacking(true);
        return perm;
    }
}
