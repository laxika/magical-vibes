package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HeartwoodTreefolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RangerEnVec.class, HeartwoodTreefolk.class})
class RangerEnVecTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the {G} ability grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        Permanent ranger = addCreatureReady(player1, new RangerEnVec());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ranger.getRegenerationShield()).isEqualTo(1);
        assertThat(ranger.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the regeneration ability without green mana")
    void cannotActivateWithoutGreenMana() {
        addCreatureReady(player1, new RangerEnVec());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration shield saves Ranger en-Vec from lethal combat damage")
    void regenSavesFromLethalCombat() {
        Permanent perm = addCreatureReady(player1, new RangerEnVec());
        perm.setRegenerationShield(1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addHeartwoodTreefolkReady(player2, 5, 5);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Ranger en-Vec");
        Permanent ranger = findPermanent(player1, "Ranger en-Vec");
        assertThat(ranger.isTapped()).isTrue();
        assertThat(ranger.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ranger en-Vec dies to lethal combat damage without a shield")
    void diesWithoutRegenShield() {
        Permanent perm = addCreatureReady(player1, new RangerEnVec());
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addHeartwoodTreefolkReady(player2, 5, 5);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Ranger en-Vec");
        harness.assertInGraveyard(player1, "Ranger en-Vec");
    }

    @Test
    @DisplayName("First strike kills the blocker before it deals damage back")
    void firstStrikeKillsBlockerFirst() {
        Permanent ranger = addCreatureReady(player1, new RangerEnVec());
        ranger.setAttacking(true);

        Permanent blocker = addHeartwoodTreefolkReady(player2, 2, 2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Ranger en-Vec");
        harness.assertNotOnBattlefield(player2, "Heartwood Treefolk");
    }

    private Permanent addHeartwoodTreefolkReady(Player player, int power, int toughness) {
        HeartwoodTreefolk card = new HeartwoodTreefolk();
        card.setPower(power);
        card.setToughness(toughness);
        return addCreatureReady(player, card);
    }
}
