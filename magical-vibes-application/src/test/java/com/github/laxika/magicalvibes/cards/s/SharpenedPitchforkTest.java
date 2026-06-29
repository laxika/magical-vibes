package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SharpenedPitchforkTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Sharpened Pitchfork has static first strike keyword grant effect")
    void hasFirstStrikeGrantEffect() {
        SharpenedPitchfork card = new SharpenedPitchfork();

        GrantKeywordEffect grant = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof GrantKeywordEffect)
                .map(e -> (GrantKeywordEffect) e)
                .filter(e -> e.keywords().contains(Keyword.FIRST_STRIKE))
                .findFirst().orElseThrow();
        assertThat(grant.scope()).isEqualTo(GrantScope.EQUIPPED_CREATURE);
        assertThat(grant.filter()).isNull();
    }

    @Test
    @DisplayName("Sharpened Pitchfork has conditional +1/+1 boost for Humans")
    void hasConditionalHumanBoostEffect() {
        SharpenedPitchfork card = new SharpenedPitchfork();

        StaticBoostEffect boost = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof StaticBoostEffect)
                .map(e -> (StaticBoostEffect) e)
                .findFirst().orElseThrow();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(1);
        assertThat(boost.scope()).isEqualTo(GrantScope.EQUIPPED_CREATURE);
        assertThat(boost.filter()).isInstanceOf(PermanentHasSubtypePredicate.class);
        assertThat(((PermanentHasSubtypePredicate) boost.filter()).subtype()).isEqualTo(CardSubtype.HUMAN);
    }

    // ===== Static effects: first strike =====

    @Test
    @DisplayName("Equipped creature has first strike regardless of creature type")
    void equippedCreatureHasFirstStrike() {
        Permanent creature = addReadyCreature(player1);
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
        Permanent creature = addReadyCreature(player1);
        Permanent pitchfork = addPitchforkReady(player1);
        pitchfork.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(pitchfork);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Static effects: conditional +1/+1 for Humans =====

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
        Permanent creature = addReadyCreature(player1);
        Permanent pitchfork = addPitchforkReady(player1);
        pitchfork.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);    // 2 + 0
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 + 0
    }

    // ===== First strike in combat =====

    @Test
    @DisplayName("Equipped creature deals first strike damage before regular damage")
    void equippedCreatureDealsFirstStrikeDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player1);
        Permanent pitchfork = addPitchforkReady(player1);
        pitchfork.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        // Non-Human creature has 2 power (no boost), player2 takes 2: 20 - 2 = 18
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Re-equip =====

    @Test
    @DisplayName("Moving Pitchfork from Human to non-Human removes +1/+1 but keeps first strike")
    void movingFromHumanToNonHumanRemovesBoostKeepsFirstStrike() {
        Permanent pitchfork = addPitchforkReady(player1);
        Permanent human = addReadyHuman(player1);
        Permanent creature = addReadyCreature(player1);
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
        Permanent creature = addReadyCreature(player1);
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

    // ===== Helpers =====

    private Permanent addPitchforkReady(Player player) {
        Permanent perm = new Permanent(new SharpenedPitchfork());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyHuman(Player player) {
        Permanent perm = new Permanent(new EliteVanguard());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
