package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CloakOfMists;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.h.HeatRay;
import com.github.laxika.magicalvibes.cards.w.WizardMentor;
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

@CardUsed({Guma.class, CloakOfMists.class, CoralMerfolk.class, HeatRay.class, WizardMentor.class})
class GumaTest extends BaseCardTest {

    @Test
    @DisplayName("Blue creature cannot block Guma")
    void blueCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new Guma());
        attacker.setAttacking(true);

        addCreatureReady(player2, new CoralMerfolk());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Guma takes no combat damage from a blue creature")
    void takesNoDamageFromBlue() {
        Permanent attacker = addCreatureReady(player1, new CoralMerfolk());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new Guma());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Guma");
        harness.assertInGraveyard(player1, "Coral Merfolk");
    }

    @Test
    @DisplayName("Guma cannot be targeted by a blue Aura")
    void cannotBeTargetedByBlueAura() {
        Permanent guma = addCreatureReady(player2, new Guma());

        harness.setHand(player1, List.of(new CloakOfMists()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, guma.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("Guma cannot be targeted by a blue activated ability")
    void cannotBeTargetedByBlueAbility() {
        addCreatureReady(player2, new WizardMentor());
        Permanent guma = addCreatureReady(player2, new Guma());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, guma.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("Guma can be targeted and damaged by a red instant")
    void canBeTargetedByRedInstant() {
        Permanent guma = addCreatureReady(player2, new Guma());

        harness.setHand(player1, List.of(new HeatRay()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantForX(player1, 0, 1, List.of(guma.getId()));
        harness.passBothPriorities();

        assertThat(guma.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Guma");
    }
}
