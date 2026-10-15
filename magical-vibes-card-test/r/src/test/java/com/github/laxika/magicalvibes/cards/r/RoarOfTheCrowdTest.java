package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.b.BurrentonBombardier;
import com.github.laxika.magicalvibes.cards.m.MothdustChangeling;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({RoarOfTheCrowd.class, BurrentonBombardier.class, MothdustChangeling.class,
        Bitterblossom.class})
class RoarOfTheCrowdTest extends BaseCardTest {

    private void handSpellAndMana() {
        harness.setHand(player1, List.of(new RoarOfTheCrowd()));
        harness.addMana(player1, ManaColor.RED, 4);
    }

    @Test
    @DisplayName("Deals damage to target player equal to the number of permanents of the chosen type you control")
    void dealsDamageToPlayerEqualToChosenTypeCount() {
        harness.addToBattlefield(player1, new BurrentonBombardier());
        harness.addToBattlefield(player1, new BurrentonBombardier());
        harness.setLife(player2, 20);
        handSpellAndMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, CardSubtype.KITHKIN.name());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Lethal damage from the chosen-type count destroys a target creature")
    void lethalCountKillsTargetCreature() {
        harness.addToBattlefield(player1, new BurrentonBombardier());
        harness.addToBattlefield(player1, new BurrentonBombardier());
        harness.addToBattlefield(player2, new BurrentonBombardier());
        handSpellAndMana();

        UUID targetId = harness.getPermanentId(player2, "Burrenton Bombardier");
        harness.castAndResolveSorcery(player1, 0, targetId);
        harness.handleListChoice(player1, CardSubtype.KITHKIN.name());

        harness.assertNotOnBattlefield(player2, "Burrenton Bombardier");
        harness.assertInGraveyard(player2, "Burrenton Bombardier");
    }

    @Test
    @DisplayName("Choosing a type you control none of deals no damage")
    void chosenTypeYouControlNoneDealsZero() {
        harness.addToBattlefield(player1, new BurrentonBombardier());
        harness.setLife(player2, 20);
        handSpellAndMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not count matching permanents controlled by an opponent")
    void doesNotCountOpponentsPermanents() {
        harness.addToBattlefield(player2, new BurrentonBombardier());
        harness.setLife(player2, 20);
        handSpellAndMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, CardSubtype.KITHKIN.name());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A Changeling you control counts as the chosen type")
    void changelingCountsAsChosenType() {
        harness.addToBattlefield(player1, new MothdustChangeling());
        harness.setLife(player2, 20);
        handSpellAndMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Counts a noncreature Kindred permanent of the chosen type")
    void countsNoncreaturePermanentOfChosenType() {
        harness.addToBattlefield(player1, new Bitterblossom());
        harness.setLife(player2, 20);
        handSpellAndMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, CardSubtype.FAERIE.name());

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Can target its controller and counts each matching permanent only once")
    void canDamageControllerWithMixedMatchingPermanents() {
        harness.addToBattlefield(player1, new Bitterblossom());
        harness.addToBattlefield(player1, new MothdustChangeling());
        harness.addToBattlefield(player1, new BurrentonBombardier());
        harness.setLife(player1, 20);
        handSpellAndMana();

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleListChoice(player1, CardSubtype.FAERIE.name());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Counts permanents present at resolution rather than at casting")
    void countsPermanentsAtResolution() {
        harness.addToBattlefield(player1, new BurrentonBombardier());
        harness.setLife(player2, 20);
        handSpellAndMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new BurrentonBombardier());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.SOLDIER.name());

        harness.assertLife(player2, 18);
    }
}
