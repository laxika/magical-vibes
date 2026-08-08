package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TerashisVerdictTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target attacking creature with power 3 or less")
    void destroysSmallAttacker() {
        Permanent attacker = addAttacker(player2, new GrizzlyBears());

        cast(attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an attacking creature with power 4")
    void cannotTargetBigAttacker() {
        Permanent attacker = addAttacker(player2, new AirElemental());

        prepare();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a small creature that is not attacking")
    void cannotTargetNonAttacker() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        prepare();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepare() {
        harness.setHand(player1, List.of(new TerashisVerdict()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void cast(UUID targetId) {
        prepare();
        harness.castInstant(player1, 0, targetId);
    }

    private Permanent addAttacker(Player owner, Card creature) {
        harness.addToBattlefield(owner, creature);
        Permanent attacker = findPermanent(owner, creature.getName());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
