package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class UmbraMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Grants umbra armor to an Aura attached to a permanent you control")
    void grantsUmbraArmorToAuraAttachedToOwnPermanent() {
        harness.addToBattlefield(player1, new UmbraMystic());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        aura.setAttachedTo(creature.getId());

        destroyWithDoomBlade(player2, creature);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Pacifism");
    }

    @Test
    @DisplayName("Does not grant umbra armor to an Aura attached to an opponent's permanent")
    void doesNotGrantUmbraArmorToAuraAttachedToOpponentsPermanent() {
        harness.addToBattlefield(player1, new UmbraMystic());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        aura.setAttachedTo(creature.getId());

        destroyWithDoomBlade(player1, creature);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private void destroyWithDoomBlade(com.github.laxika.magicalvibes.model.Player caster,
                                      Permanent target) {
        harness.setHand(caster, List.of(new DoomBlade()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.forceActivePlayer(caster);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
