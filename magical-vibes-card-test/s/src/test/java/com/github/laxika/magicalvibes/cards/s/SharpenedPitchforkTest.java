package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SharpenedPitchfork.class, SnapcasterMage.class, WalkingCorpse.class})
class SharpenedPitchforkTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has first strike regardless of creature type")
    void equippedCreatureHasFirstStrike() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent pitchfork = addPitchforkReady(player1);
        pitchfork.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Equipped Human creature has first strike")
    void equippedHumanHasFirstStrike() {
        Permanent human = addReadyHuman(player1);
        Permanent pitchfork = addPitchforkReady(player1);
        pitchfork.setAttachedTo(human.getId());

        assertThat(gqs.hasKeyword(gd, human, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses first strike when Pitchfork is removed")
    void creatureLosesFirstStrikeWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent pitchfork = addPitchforkReady(player1);
        pitchfork.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(pitchfork);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Equipped Human creature gets +1/+1")
    void equippedHumanGetsBoost() {
        Permanent human = addReadyHuman(player1);
        Permanent pitchfork = addPitchforkReady(player1);
        pitchfork.setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(3);     // 2 + 1
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(2); // 1 + 1
    }

    @Test
    @DisplayName("Equipped non-Human creature does not get +1/+1")
    void equippedNonHumanDoesNotGetBoost() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent pitchfork = addPitchforkReady(player1);
        pitchfork.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);    // 2 + 0
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 + 0
    }

    @Test
    @DisplayName("Equipped creature deals first strike damage before regular damage")
    void equippedCreatureDealsFirstStrikeDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent pitchfork = addPitchforkReady(player1);
        pitchfork.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        // Non-Human creature has 2 power (no boost), player2 takes 2: 20 - 2 = 18
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Moving Pitchfork from Human to non-Human removes +1/+1 but keeps first strike")
    void movingFromHumanToNonHumanRemovesBoostKeepsFirstStrike() {
        Permanent pitchfork = addPitchforkReady(player1);
        Permanent human = addReadyHuman(player1);
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        pitchfork.setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, human, Keyword.FIRST_STRIKE)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(pitchfork.getAttachedTo()).isEqualTo(creature.getId());
        // Human loses all bonuses
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, human, Keyword.FIRST_STRIKE)).isFalse();
        // Non-Human gets first strike but no +1/+1
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Moving Pitchfork from non-Human to Human grants +1/+1")
    void movingFromNonHumanToHumanGrantsBoost() {
        Permanent pitchfork = addPitchforkReady(player1);
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent human = addReadyHuman(player1);
        pitchfork.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, human.getId());
        harness.passBothPriorities();

        assertThat(pitchfork.getAttachedTo()).isEqualTo(human.getId());
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, human, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature kills its blocker before regular combat damage")
    void firstStrikeKillsBlockerBeforeItCanDealDamage() {
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        Permanent pitchfork = addPitchforkReady(player1);
        pitchfork.setAttachedTo(attacker.getId());
        addCreatureReady(player2, new WalkingCorpse());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        harness.assertInGraveyard(player2, "Walking Corpse");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Unequipped Pitchfork grants no bonuses to nearby creatures")
    void unequippedPitchforkGrantsNoBonuses() {
        addPitchforkReady(player1);
        Permanent human = addReadyHuman(player1);

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, human, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addPitchforkReady(Player player) {
        return addCreatureReady(player, new SharpenedPitchfork());
    }

    private Permanent addReadyHuman(Player player) {
        return addCreatureReady(player, new SnapcasterMage());
    }
}
