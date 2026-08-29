package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReclusiveArtificerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals damage equal to the number of artifacts controlled")
    void etbDealsDamageEqualToArtifactCount() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndAcceptMay(bears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("With one artifact the damage is only 1 and the creature survives")
    void damageScalesWithArtifactCount() {
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndAcceptMay(bears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the may deals no damage")
    void decliningMayDealsNoDamage() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castArtificer();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve the ETB trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(bears.getMarkedDamage()).isZero();
    }

    private void castAndAcceptMay(UUID targetId) {
        castArtificer();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities(); // resolve the ETB trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);
    }

    private void castArtificer() {
        harness.setHand(player1, List.of(new ReclusiveArtificer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature spell
    }
}
