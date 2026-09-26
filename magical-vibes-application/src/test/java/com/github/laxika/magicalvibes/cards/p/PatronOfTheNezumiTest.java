package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BakuAltar;
import com.github.laxika.magicalvibes.cards.h.HerosDemise;
import com.github.laxika.magicalvibes.cards.i.IwamoriOfTheOpenFist;
import com.github.laxika.magicalvibes.cards.n.NezumiShadowWatcher;
import com.github.laxika.magicalvibes.cards.t.TerashisGrasp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        PatronOfTheNezumi.class,
        BakuAltar.class,
        HerosDemise.class,
        IwamoriOfTheOpenFist.class,
        NezumiShadowWatcher.class,
        TerashisGrasp.class
})
class PatronOfTheNezumiTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent loses 1 life when their permanent is put into their graveyard")
    void opponentLosesLifeWhenTheirPermanentDies() {
        harness.addToBattlefield(player1, new PatronOfTheNezumi());
        harness.addToBattlefield(player2, new BakuAltar());
        UUID bakuAltarId = harness.getPermanentId(player2, "Baku Altar");
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new TerashisGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, bakuAltarId);
        harness.passBothPriorities(); // trigger resolves

        harness.assertInGraveyard(player2, "Baku Altar");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Controller loses no life when their own permanent is put into their graveyard")
    void controllerLosesNoLifeForOwnPermanent() {
        harness.addToBattlefield(player1, new PatronOfTheNezumi());
        harness.addToBattlefield(player1, new BakuAltar());
        UUID bakuAltarId = harness.getPermanentId(player1, "Baku Altar");
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player2, List.of(new TerashisGrasp()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, bakuAltarId);

        harness.assertInGraveyard(player1, "Baku Altar");
        harness.assertLife(player1, 20);
        // Terashi's Grasp gains life equal to Baku Altar's mana value (2).
        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Triggers for any permanent type, including a dying creature")
    void triggersForDyingCreature() {
        harness.addToBattlefield(player1, new PatronOfTheNezumi());
        harness.addToBattlefield(player2, new IwamoriOfTheOpenFist());
        UUID iwamoriId = harness.getPermanentId(player2, "Iwamori of the Open Fist");
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new HerosDemise()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, iwamoriId);
        harness.passBothPriorities(); // trigger resolves

        harness.assertInGraveyard(player2, "Iwamori of the Open Fist");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Rat offering sacrifices a Rat and pays the difference in mana costs")
    void castsWithRatOffering() {
        Permanent rat = harness.addToBattlefieldAndReturn(player1, new NezumiShadowWatcher());
        harness.setHand(player1, List.of(new PatronOfTheNezumi()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(rat.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Patron of the Nezumi");
        harness.assertNotOnBattlefield(player1, "Nezumi Shadow-Watcher");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
