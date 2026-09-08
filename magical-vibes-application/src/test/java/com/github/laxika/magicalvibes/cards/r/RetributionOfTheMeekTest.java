package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.Archangel;
import com.github.laxika.magicalvibes.cards.d.DarajaGriffin;
import com.github.laxika.magicalvibes.cards.g.GiantCaterpillar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({RetributionOfTheMeek.class, Archangel.class, DarajaGriffin.class, GiantCaterpillar.class})
class RetributionOfTheMeekTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys creatures with power 4 or greater on both sides")
    void destroysPowerFourOrGreater() {
        harness.addToBattlefield(player1, new Archangel());
        harness.addToBattlefield(player2, new Archangel());
        harness.setHand(player1, List.of(new RetributionOfTheMeek()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Archangel");
        harness.assertNotOnBattlefield(player2, "Archangel");
        harness.assertInGraveyard(player1, "Archangel");
        harness.assertInGraveyard(player2, "Archangel");
    }

    @Test
    @DisplayName("Leaves creatures with power 3 or less untouched")
    void leavesSmallerCreatures() {
        harness.addToBattlefield(player1, new DarajaGriffin());
        harness.addToBattlefield(player1, new GiantCaterpillar());
        harness.setHand(player1, List.of(new RetributionOfTheMeek()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Daraja Griffin");
        harness.assertOnBattlefield(player1, "Giant Caterpillar");
    }

    @Test
    @DisplayName("Destroys only power-4+ creatures among a mixed board")
    void destroysOnlyPowerFourOrGreaterAmongMixed() {
        harness.addToBattlefield(player1, new Archangel());
        harness.addToBattlefield(player1, new GiantCaterpillar());
        harness.setHand(player1, List.of(new RetributionOfTheMeek()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Archangel");
        harness.assertOnBattlefield(player1, "Giant Caterpillar");
    }

    @Test
    @DisplayName("Destroyed creatures can't be regenerated")
    void cannotBeRegenerated() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new Archangel());
        elemental.setRegenerationShield(1);

        harness.setHand(player1, List.of(new RetributionOfTheMeek()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Archangel");
        harness.assertInGraveyard(player2, "Archangel");
    }
}
