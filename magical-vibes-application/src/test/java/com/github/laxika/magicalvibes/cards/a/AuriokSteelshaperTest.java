package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuriokSteelshaperTest extends BaseCardTest {

    @Test
    @DisplayName("Soldiers and Knights you control get +1/+1 only while Auriok Steelshaper is equipped")
    void buffsControlledSoldiersAndKnightsWhileEquipped() {
        harness.addToBattlefield(player1, new AuriokSteelshaper());
        Permanent steelshaper = findPermanent(player1, "Auriok Steelshaper");
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BlackKnight());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentKnight = harness.addToBattlefieldAndReturn(player2, new BlackKnight());

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);

        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        scimitar.setAttachedTo(steelshaper.getId());

        assertThat(gqs.getEffectivePower(gd, steelshaper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, steelshaper)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentKnight)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip abilities of Equipment you control cost {1} less")
    void reducesEquipCost() {
        harness.addToBattlefield(player1, new AuriokSteelshaper());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scimitar), null, creature.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(creature.getId());
    }
}
