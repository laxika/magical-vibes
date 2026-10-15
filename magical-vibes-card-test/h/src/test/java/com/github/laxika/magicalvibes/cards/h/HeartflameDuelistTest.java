package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CutIn;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeartflameDuelist.class, HeartflameSlash.class, CutIn.class, Shock.class, TurnToFrog.class})
class HeartflameDuelistTest extends BaseCardTest {

    @Test
    void adventureDealsThreeDamageAndExilesTheCard() {
        HeartflameDuelist card = new HeartflameDuelist();
        harness.setHand(player1, List.of(card));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        HeartflameDuelist card = new HeartflameDuelist();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Heartflame Duelist");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void controllerInstantGainsLifelink() {
        harness.addToBattlefield(player1, new HeartflameDuelist());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    void controllerSorceryGainsLifeForFullDamageToCreature() {
        harness.addToBattlefield(player1, new HeartflameDuelist());
        var target = harness.addToBattlefieldAndReturn(player2, new HeartflameDuelist());
        harness.setHand(player1, List.of(new CutIn()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));

        harness.assertLife(player1, 24);
        harness.assertInGraveyard(player2, "Heartflame Duelist");
    }

    @Test
    void adventureGainsLifelinkFromAnotherDuelist() {
        harness.addToBattlefield(player1, new HeartflameDuelist());
        HeartflameDuelist card = new HeartflameDuelist();
        harness.setHand(player1, List.of(card));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void opposingInstantDoesNotGainLifelink() {
        harness.addToBattlefield(player1, new HeartflameDuelist());
        harness.setHand(player2, List.of(new Shock()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void damageToControllerWithLifelinkLeavesLifeUnchanged() {
        harness.addToBattlefield(player1, new HeartflameDuelist());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 20);
    }

    @Test
    void multipleDuelistsDoNotMultiplyLifelink() {
        harness.addToBattlefield(player1, new HeartflameDuelist());
        harness.addToBattlefield(player1, new HeartflameDuelist());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void losingAllAbilitiesStopsGrantingSpellLifelink() {
        var duelist = harness.addToBattlefieldAndReturn(player1, new HeartflameDuelist());
        harness.setHand(player1, List.of(new TurnToFrog(), new Shock()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, duelist.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    void adventureWithIllegalTargetGoesToGraveyardWithoutPermission() {
        var target = harness.addToBattlefieldAndReturn(player2, new HeartflameDuelist());
        HeartflameDuelist card = new HeartflameDuelist();
        harness.setHand(player1, List.of(card, new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Heartflame Duelist");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }
}
