package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManaShort.class, Forest.class, GrizzlyBears.class})
class ManaShortTest extends BaseCardTest {

    @Test
    @DisplayName("Taps all lands target player controls")
    void tapsAllLands() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(battlefield).allMatch(p -> !p.isTapped());

        castAndResolve(player2.getId());

        assertThat(battlefield).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Target player loses all unspent mana")
    void emptiesTargetManaPool() {
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(5);

        castAndResolve(player2.getId());

        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Does not tap non-land permanents")
    void doesNotTapNonLands() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(player2.getId());

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not tap caster's lands when targeting opponent")
    void doesNotAffectCaster() {
        Permanent casterLand = harness.addToBattlefieldAndReturn(player1, new Forest());

        castAndResolve(player2.getId());

        assertThat(casterLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target the caster and affects only that player's lands and mana")
    void affectsCasterWhenSelfTargeted() {
        Permanent casterLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.GREEN, 2);

        castAndResolve(player1.getId());

        assertThat(casterLand.isTapped()).isTrue();
        assertThat(opponentLand.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    @DisplayName("Clears mana added after the spell is cast but before it resolves")
    void clearsManaAtResolution() {
        harness.setHand(player1, List.of(new ManaShort()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Clears persistent mana as well as ordinary mana")
    void clearsPersistentMana() {
        ManaPool targetPool = gd.playerManaPools.get(player2.getId());
        targetPool.addPersistentMana(ManaColor.GREEN, 2);
        targetPool.add(ManaColor.BLUE, 1);

        castAndResolve(player2.getId());

        assertThat(targetPool.getTotalAllMana()).isZero();
        assertThat(targetPool.getPersistentMana(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent nonPlayerTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ManaShort()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonPlayerTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only target players");
    }

    private void castAndResolve(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new ManaShort()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, targetPlayerId);
    }
}
