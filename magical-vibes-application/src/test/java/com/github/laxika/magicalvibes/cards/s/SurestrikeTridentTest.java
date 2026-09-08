package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SurestrikeTridentTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gains first strike")
    void equippedCreatureGainsFirstStrike() {
        Permanent creature = addReadyCreature(player1);
        Permanent trident = addReadyTrident(player1);
        trident.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Unequipped creature does not gain first strike")
    void unequippedCreatureDoesNotGainFirstStrike() {
        Permanent creature = addReadyCreature(player1);
        addReadyTrident(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Equipped creature can unattach the Trident and deal damage equal to its power")
    void unattachAndDealDamageEqualToPower() {
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player1);
        Permanent trident = addReadyTrident(player1);
        trident.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());

        assertThat(creature.isTapped()).isTrue();
        assertThat(trident.getAttachedTo()).isNull();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Trident ability cannot target a creature")
    void abilityCannotTargetCreature() {
        Permanent creature = addReadyCreature(player1);
        Permanent trident = addReadyTrident(player1);
        trident.setAttachedTo(creature.getId());
        Permanent target = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("planeswalker or player");
    }

    private Permanent addReadyCreature(Player player) {
        return addReady(player, new GrizzlyBears());
    }

    private Permanent addReadyTrident(Player player) {
        return addReady(player, new SurestrikeTrident());
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
