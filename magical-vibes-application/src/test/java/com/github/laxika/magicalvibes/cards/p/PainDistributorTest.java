package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PainDistributor.class, Shock.class, MindStone.class, Naturalize.class})
class PainDistributorTest extends BaseCardTest {

    @Test
    void createsTreasureForEachPlayersFirstSpell() {
        addCreatureReady(player1, new PainDistributor());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        resolveAllTriggers();
        harness.castAndResolveInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
    }

    @Test
    void treasureIsCreatedBeforeTheTriggeringSpellResolves() {
        addCreatureReady(player1, new PainDistributor());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.assertLife(player1, 20);

        resolveAllTriggers();
        harness.assertLife(player1, 18);
    }

    @Test
    void doesNotCreateTreasureForSecondSpellWhenEnteringAfterFirstSpell() {
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        addCreatureReady(player1, new PainDistributor());

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void sacrificingAnOpponentsArtifactDealsDamage() {
        addCreatureReady(player1, new PainDistributor());
        harness.addToBattlefield(player2, new MindStone());
        harness.setLibrary(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, 1, null, null);
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Mind Stone");
        harness.assertInHand(player2, "Shock");
    }

    @Test
    void destroyingAnOpponentsTreasureTokenDealsDamage() {
        addCreatureReady(player2, new PainDistributor());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        resolveAllTriggers();
        addCreatureReady(player1, new PainDistributor());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Treasure"));
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void dealsDamageToTheControllerOfAnOpponentsArtifact() {
        addCreatureReady(player1, new PainDistributor());
        harness.addToBattlefield(player2, new MindStone());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, mindStoneId);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    void doesNotTriggerForAnArtifactControlledByItsController() {
        addCreatureReady(player1, new PainDistributor());
        harness.addToBattlefield(player1, new MindStone());
        harness.setLife(player1, 20);

        UUID mindStoneId = harness.getPermanentId(player1, "Mind Stone");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, mindStoneId);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }
}
