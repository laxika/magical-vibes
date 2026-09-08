package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.r.RabidBite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class DinosaurHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a Dinosaur it deals damage to")
    void destroysDamagedDinosaur() {
        harness.addToBattlefield(player1, new DinosaurHunter());
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, List.of(
                harness.getPermanentId(player1, "Dinosaur Hunter"),
                harness.getPermanentId(player2, "Colossal Dreadmaw")));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Dinosaur Hunter");
        harness.assertInGraveyard(player2, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("Does not destroy a non-Dinosaur it deals damage to")
    void doesNotDestroyNonDinosaur() {
        harness.addToBattlefield(player1, new DinosaurHunter());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, List.of(
                harness.getPermanentId(player1, "Dinosaur Hunter"),
                harness.getPermanentId(player2, "Air Elemental")));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Dinosaur Hunter");
        harness.assertOnBattlefield(player2, "Air Elemental");
    }
}
