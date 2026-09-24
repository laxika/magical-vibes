package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.ChitteringRats;
import com.github.laxika.magicalvibes.cards.e.EchoingDecay;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhispersilkCloak.class, ChitteringRats.class, EchoingDecay.class})
class WhispersilkCloakTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Whispersilk Cloak puts it on the battlefield unattached")
    void castingPutsOnBattlefield() {
        harness.castFromHand(player1, new WhispersilkCloak(), "{3}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof WhispersilkCloak
                        && !p.isAttached());
    }

    @Test
    @DisplayName("Resolving equip ability attaches Cloak to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent cloak = addCloakReady(player1);
        Permanent creature = addCreatureReady(player1, new ChitteringRats());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(cloak.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Equipped creature has shroud")
    void equippedCreatureHasShroud() {
        Permanent creature = addCreatureReady(player1, new ChitteringRats());
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Creature loses shroud when Cloak is removed")
    void creatureLosesShroudWhenCloakRemoved() {
        Permanent creature = addCreatureReady(player1, new ChitteringRats());
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(cloak);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Unequipped creature does not have shroud from Cloak")
    void unequippedCreatureDoesNotHaveShroud() {
        Permanent creature = addCreatureReady(player1, new ChitteringRats());
        addCloakReady(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud prevents a spell from targeting the equipped creature")
    void shroudPreventsTargetingBySpell() {
        Permanent creature = addCreatureReady(player1, new ChitteringRats());
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new EchoingDecay()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud prevents an equip ability from targeting the equipped creature")
    void shroudPreventsTargetingByEquipAbility() {
        Permanent cloak = addCloakReady(player1);
        Permanent creature = addCreatureReady(player1, new ChitteringRats());
        cloak.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipCannotTargetOpponentsCreature() {
        addCloakReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new ChitteringRats());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Equip is sorcery speed")
    void equipIsSorcerySpeed() {
        Permanent cloak = addCloakReady(player1);
        Permanent creature = addCreatureReady(player1, new ChitteringRats());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int cloakIndex = gd.playerBattlefields.get(player1.getId()).indexOf(cloak);
        assertThatThrownBy(() -> harness.activateAbility(player1, cloakIndex, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Equipped creature can't be blocked")
    void equippedCreatureCantBeBlocked() {
        Permanent creature = addCreatureReady(player1, new ChitteringRats());
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(creature.getId());

        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("Creature loses can't-be-blocked when Cloak is removed")
    void creatureLosesCantBeBlockedWhenCloakRemoved() {
        Permanent creature = addCreatureReady(player1, new ChitteringRats());
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(creature.getId());

        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(cloak);

        assertThat(gqs.hasCantBeBlocked(gd, creature)).isFalse();
    }

    @Test
    @DisplayName("Unequipped creature is not unblockable from Cloak")
    void unequippedCreatureIsNotUnblockable() {
        Permanent creature = addCreatureReady(player1, new ChitteringRats());
        addCloakReady(player1);

        assertThat(gqs.hasCantBeBlocked(gd, creature)).isFalse();
    }

    @Test
    @DisplayName("Equipped creature attacks and cannot be assigned blockers")
    void equippedCreatureCannotBeBlocked() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new ChitteringRats());
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new ChitteringRats());

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareAttackersAndPrepareBlockers(List.of(attackerIndex));
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        assertThatThrownBy(() -> gs.declareBlockers(
                        gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cloak can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent cloak = addCloakReady(player1);
        Permanent creature1 = addCreatureReady(player1, new ChitteringRats());
        Permanent creature2 = addCreatureReady(player1, new ChitteringRats());

        cloak.setAttachedTo(creature1.getId());
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, creature1)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(cloak.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, creature1)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, creature2)).isTrue();
    }

    private Permanent addCloakReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new WhispersilkCloak());
        perm.setSummoningSick(false);
        return perm;
    }
}
