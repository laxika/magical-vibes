package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.m.MomentOfHeroism;
import com.github.laxika.magicalvibes.cards.v.VictimOfNight;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InvisibleStalker.class, WalkingCorpse.class, MomentOfHeroism.class, VictimOfNight.class})
class InvisibleStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Invisible Stalker cannot be blocked")
    void cannotBeBlocked() {
        addCreatureReady(player2, new WalkingCorpse());
        Permanent atkPerm = addCreatureReady(player1, new InvisibleStalker());
        atkPerm.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Unblocked Invisible Stalker deals 1 damage to defending player")
    void dealsOneDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent atkPerm = addCreatureReady(player1, new InvisibleStalker());
        atkPerm.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Hexproof prevents an opponent from targeting Invisible Stalker")
    void opponentCannotTargetStalker() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new InvisibleStalker());
        harness.setHand(player2, List.of(new VictimOfNight()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, stalker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        harness.assertOnBattlefield(player1, "Invisible Stalker");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Its controller can target Invisible Stalker with a combat trick")
    void controllerCanTargetStalker() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new InvisibleStalker());
        harness.setHand(player1, List.of(new MomentOfHeroism()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, stalker.getId());

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Hexproof does not prevent its controller from destroying Invisible Stalker")
    void controllerCanDestroyStalker() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new InvisibleStalker());
        harness.setHand(player1, List.of(new VictimOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, stalker.getId());

        harness.assertNotOnBattlefield(player1, "Invisible Stalker");
        harness.assertInGraveyard(player1, "Invisible Stalker");
    }
}
