package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BurningCloakTest extends BaseCardTest {

    private void prepare() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BurningCloak()));
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private Permanent battlefieldPermanent(String name) {
        return gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Grants +2/+0 and deals 2 damage to the surviving target")
    void boostsAndDamages() {
        prepare();
        harness.addToBattlefield(player2, new AirElemental()); // 4/4

        UUID targetId = battlefieldPermanent("Air Elemental").getId();
        harness.castSorcery(player1, 0, 0, targetId);
        harness.passBothPriorities();

        Permanent target = battlefieldPermanent("Air Elemental");
        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The 2 damage destroys a creature with 2 or less toughness")
    void killsSmallCreature() {
        prepare();
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2, +2/+0 keeps toughness at 2

        UUID targetId = battlefieldPermanent("Grizzly Bears").getId();
        harness.castSorcery(player1, 0, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("The +2/+0 wears off at cleanup")
    void boostWearsOff() {
        prepare();
        harness.addToBattlefield(player2, new AirElemental()); // 4/4

        UUID targetId = battlefieldPermanent("Air Elemental").getId();
        harness.castSorcery(player1, 0, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent target = battlefieldPermanent("Air Elemental");
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        prepare();
        harness.addToBattlefield(player2, new Forest());

        UUID landId = battlefieldPermanent("Forest").getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }
}
