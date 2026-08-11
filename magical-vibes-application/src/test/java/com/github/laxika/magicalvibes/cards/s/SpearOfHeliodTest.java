package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpearOfHeliodTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts creatures you control, but not creatures controlled by an opponent")
    void boostsOwnCreaturesOnly() {
        harness.addToBattlefield(player1, new SpearOfHeliod());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Destroys a creature that dealt damage to you this turn")
    void destroysCreatureThatDealtDamageToYou() {
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new SpearOfHeliod());
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());

        harness.activateAbility(player2, indexOf(player2, sorcerer), null, player1.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, spear), null, sorcerer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(sorcerer);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(sorcerer.getCard().getId()));
        assertThat(spear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature that did not deal damage to you this turn")
    void cannotTargetCreatureThatDidNotDealDamageToYou() {
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new SpearOfHeliod());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, spear), null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
