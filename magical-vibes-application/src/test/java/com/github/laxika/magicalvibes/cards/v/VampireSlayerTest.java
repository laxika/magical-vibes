package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BaronSengir;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.r.RabidBite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({VampireSlayer.class, BaronSengir.class, GiantSpider.class, RabidBite.class})
class VampireSlayerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a Vampire it deals damage to")
    void destroysDamagedVampire() {
        harness.addToBattlefield(player1, new VampireSlayer());
        harness.addToBattlefield(player2, new BaronSengir());
        castRabidBite("Baron Sengir");

        harness.assertInGraveyard(player2, "Baron Sengir");
    }

    @Test
    @DisplayName("Does not destroy a non-Vampire it deals damage to")
    void doesNotDestroyNonVampire() {
        harness.addToBattlefield(player1, new VampireSlayer());
        harness.addToBattlefield(player2, new GiantSpider());
        castRabidBite("Giant Spider");

        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    private void castRabidBite(String targetName) {
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, List.of(
                harness.getPermanentId(player1, "Vampire Slayer"),
                harness.getPermanentId(player2, targetName)));
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
