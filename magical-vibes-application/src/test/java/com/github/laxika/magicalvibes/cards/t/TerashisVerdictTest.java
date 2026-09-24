package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FrostOgre;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TerashisVerdict.class, FrostOgre.class, GnarledMass.class})
class TerashisVerdictTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target attacking creature with power exactly 3")
    void destroysPowerThreeAttacker() {
        Permanent attacker = addAttacker(player2, new GnarledMass());

        cast(attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gnarled Mass");
        harness.assertInGraveyard(player2, "Gnarled Mass");
    }

    @Test
    @DisplayName("Cannot target an attacking creature with power greater than 3")
    void cannotTargetBigAttacker() {
        Permanent attacker = addAttacker(player2, new FrostOgre());

        prepare();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a small creature that is not attacking")
    void cannotTargetNonAttacker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());

        prepare();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not destroy the target if it stops attacking before resolution")
    void doesNotDestroyTargetThatStopsAttackingBeforeResolution() {
        Permanent attacker = addAttacker(player2, new GnarledMass());

        cast(attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Gnarled Mass");
        harness.assertNotInGraveyard(player2, "Gnarled Mass");
    }

    @Test
    @DisplayName("Does not destroy the target if its power becomes 4 before resolution")
    void doesNotDestroyTargetThatGrowsAbovePowerLimitBeforeResolution() {
        Permanent attacker = addAttacker(player2, new GnarledMass());

        cast(attacker.getId());
        attacker.setPowerModifier(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Gnarled Mass");
        harness.assertNotInGraveyard(player2, "Gnarled Mass");
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
        Permanent attacker = harness.addToBattlefieldAndReturn(owner, creature);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
