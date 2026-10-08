package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmitingHelix.class, GrizzlyBears.class})
class SmitingHelixTest extends BaseCardTest {

    @Test
    void dealsDamageToPlayerAndGainsLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SmitingHelix()));
        addManaForNormalCast();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Smiting Helix");
    }

    @Test
    void dealsDamageToCreatureAndGainsLife() {
        harness.setLife(player1, 10);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SmitingHelix()));
        addManaForNormalCast();

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void flashbackDealsDamageGainsLifeAndExilesCard() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new SmitingHelix()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertNotInGraveyard(player1, "Smiting Helix");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Smiting Helix"));
    }

    @Test
    void canTargetItsController() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new SmitingHelix()));
        addManaForNormalCast();

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 10);
        harness.assertInGraveyard(player1, "Smiting Helix");
    }

    @Test
    void flashbackCanTargetControllersCreature() {
        harness.setLife(player1, 10);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new SmitingHelix()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveFlashback(player1, 0, creature.getId());

        harness.assertLife(player1, 13);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Smiting Helix");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Smiting Helix"));
    }

    @Test
    void gainsNoLifeWhenOnlyTargetLeavesBattlefield() {
        harness.setLife(player1, 10);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SmitingHelix()));
        addManaForNormalCast();

        harness.castSorcery(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertInGraveyard(player1, "Smiting Helix");
    }

    @Test
    void flashbackExilesWithoutLifeGainWhenOnlyTargetLeavesBattlefield() {
        harness.setLife(player1, 10);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new SmitingHelix()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castFlashback(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertNotInGraveyard(player1, "Smiting Helix");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Smiting Helix"));
    }

    @Test
    void lifeGainCompletesBeforeLethalSelfDamageIsChecked() {
        harness.setLife(player1, 3);
        harness.setHand(player1, List.of(new SmitingHelix()));
        addManaForNormalCast();

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 3);
        harness.assertInGraveyard(player1, "Smiting Helix");
    }

    private void addManaForNormalCast() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
