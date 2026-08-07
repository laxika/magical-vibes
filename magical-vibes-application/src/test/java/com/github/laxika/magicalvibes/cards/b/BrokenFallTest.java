package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrokenFallTest extends BaseCardTest {

    @Test
    @DisplayName("Returns to hand as a cost and puts a regeneration shield on the target creature")
    void bouncesItselfAndRegeneratesTarget() {
        harness.addToBattlefield(player1, new BrokenFall());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, battlefieldIndex(player1, "Broken Fall"), null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getRegenerationShield()).isEqualTo(1);
        assertThat(countPermanents(player1, "Broken Fall")).isZero();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Broken Fall"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new BrokenFall());
        harness.addToBattlefield(player1, new FountainOfYouth());

        UUID artifactId = findPermanent(player1, "Fountain of Youth").getId();
        int index = battlefieldIndex(player1, "Broken Fall");

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, artifactId))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(com.github.laxika.magicalvibes.model.Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
