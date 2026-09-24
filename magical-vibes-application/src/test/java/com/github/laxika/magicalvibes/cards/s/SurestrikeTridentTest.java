package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurestrikeTrident.class, CrazedGoblin.class, NicolBolasPlaneswalker.class})
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
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Equip {4} attaches Surestrike Trident to a creature you control")
    void equipAttachesToControlledCreature() {
        Permanent trident = addReadyTrident(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(trident.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipCannotTargetOpponentsCreature() {
        Permanent trident = addReadyTrident(player1);
        Permanent opponentCreature = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(trident.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
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

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Equipped creature can target a planeswalker")
    void canTargetPlaneswalker() {
        Permanent creature = addReadyCreature(player1);
        Permanent trident = addReadyTrident(player1);
        trident.setAttachedTo(creature.getId());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
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
        assertThat(creature.isTapped()).isFalse();
        assertThat(trident.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new CrazedGoblin());
    }

    private Permanent addReadyTrident(Player player) {
        return addCreatureReady(player, new SurestrikeTrident());
    }
}
