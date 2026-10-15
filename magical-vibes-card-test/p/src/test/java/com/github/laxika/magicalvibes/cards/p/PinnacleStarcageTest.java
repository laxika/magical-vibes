package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({PinnacleStarcage.class, Forest.class, GrizzlyBears.class, MindStone.class,
        WornPowerstone.class, Naturalize.class, RestInPeace.class})
class PinnacleStarcageTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles low-mana-value artifacts and creatures, and they return when it leaves")
    void etbExilesMatchingPermanentsUntilItLeaves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new MindStone());
        harness.addToBattlefield(player2, new WornPowerstone());
        harness.addToBattlefield(player2, new Forest());

        Permanent starcage = castAndResolveStarcage();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Mind Stone");
        harness.assertOnBattlefield(player2, "Worn Powerstone");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.getCardsExiledByPermanent(starcage.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Mind Stone");

        destroyStarcage(starcage.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Mind Stone");
        assertThat(gd.getCardsExiledByPermanent(starcage.getId())).isEmpty();
    }

    @Test
    @DisplayName("Activated ability puts exiled cards into their owners' graveyards and creates one Robot per card")
    void activatedAbilityReturnsCardsCreatesRobotsAndSacrifices() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent mindStone = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Permanent starcage = castAndResolveStarcage();

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pinnacle Starcage");
        assertThat(gd.getCardsExiledByPermanent(starcage.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(bears.getCard(), mindStone.getCard());

        List<Permanent> robots = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(robots).hasSize(2);
        assertThat(robots).allSatisfy(robot -> {
            assertThat(robot.getCard().getName()).isEqualTo("Robot");
            assertThat(robot.getCard().getColor()).isNull();
            assertThat(robot.getCard().getPower()).isEqualTo(2);
            assertThat(robot.getCard().getToughness()).isEqualTo(2);
            assertThat(robot.getCard().getSubtypes()).containsExactly(CardSubtype.ROBOT);
            assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(robot.getCard().hasType(CardType.CREATURE)).isTrue();
        });
    }

    @Test
    @DisplayName("Nothing is exiled when Starcage leaves before its entrance trigger resolves")
    void removalBeforeEntranceTriggerDoesNotExile() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new MindStone());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new PinnacleStarcage(), "{1}{W}{W}");
        harness.passBothPriorities();
        Permanent starcage = findPermanent(player1, "Pinnacle Starcage");

        destroyStarcage(starcage.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Pinnacle Starcage");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Mind Stone");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Cards prevented from entering a graveyard do not produce Robots")
    void graveyardReplacementDoesNotProduceRobots() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new RestInPeace());
        Permanent starcage = castAndResolveStarcage();
        harness.assertOnBattlefield(player2, "Rest in Peace");

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pinnacle Starcage");
        harness.assertNotOnBattlefield(player1, "Robot");
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(bears.getCard());
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .contains(bears.getCard(), starcage.getCard());
        assertThat(gd.getCardsExiledByPermanent(starcage.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cards re-exiled by Rest in Peace remain exiled when Starcage is sacrificed")
    void reExiledCardsDoNotReturnOnSacrifice() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new RestInPeace());
        Permanent starcage = castAndResolveStarcage();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pinnacle Starcage");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(bears.getCard());
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .contains(bears.getCard(), starcage.getCard());
        assertThat(gd.getCardsExiledByPermanent(starcage.getId())).isEmpty();
    }

    @Test
    @DisplayName("Activating with no exiled cards still sacrifices Starcage without creating Robots")
    void emptyExileGroupStillSacrifices() {
        castAndResolveStarcage();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pinnacle Starcage");
        harness.assertNotOnBattlefield(player1, "Robot");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName()).contains("Pinnacle Starcage");
    }

    @Test
    @DisplayName("Removing Starcage in response to activation returns the cards and produces no Robots")
    void removalInResponseToActivationReturnsCards() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent starcage = castAndResolveStarcage();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, null);

        destroyStarcage(starcage.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Robot");
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(bears.getCard());
        assertThat(gd.getCardsExiledByPermanent(starcage.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiled Robot tokens cease to exist and do not produce replacement Robots")
    void exiledTokensDoNotProduceRobots() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castAndResolveStarcage();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Robot");

        Permanent starcage = castAndResolveStarcage();
        harness.assertNotOnBattlefield(player1, "Robot");
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 2);
        int starcageIndex = gd.playerBattlefields.get(player1.getId()).indexOf(starcage);
        harness.activateAbility(player1, starcageIndex, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pinnacle Starcage");
        harness.assertNotOnBattlefield(player1, "Robot");
        assertThat(gd.getCardsExiledByPermanent(starcage.getId())).isEmpty();
    }

    private Permanent castAndResolveStarcage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new PinnacleStarcage(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Pinnacle Starcage");
    }

    private void destroyStarcage(UUID starcageId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, starcageId);
    }
}
