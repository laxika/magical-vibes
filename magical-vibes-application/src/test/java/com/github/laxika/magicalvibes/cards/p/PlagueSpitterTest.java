package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Duskwalker;
import com.github.laxika.magicalvibes.cards.k.KavuTitan;
import com.github.laxika.magicalvibes.cards.p.PhyrexianLens;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlagueSpitter.class, KavuTitan.class, Duskwalker.class, PhyrexianLens.class})
class PlagueSpitterTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger deals 1 damage to each creature and each player")
    void upkeepTriggerDealsOneDamageToAllCreaturesAndPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new PlagueSpitter());
        harness.addToBattlefield(player1, new KavuTitan());
        harness.addToBattlefield(player2, new KavuTitan());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Plague Spitter").getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanents(player1, "Kavu Titan").getFirst().getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanents(player2, "Kavu Titan").getFirst().getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("When Plague Spitter dies, it deals 1 damage to each creature and each player")
    void deathTriggerDealsOneDamageToAllCreaturesAndPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent spitter = harness.addToBattlefieldAndReturn(player1, new PlagueSpitter());
        harness.addToBattlefield(player1, new KavuTitan());
        harness.addToBattlefield(player2, new KavuTitan());

        spitter.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Plague Spitter");
        assertThat(findPermanents(player1, "Kavu Titan").getFirst().getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanents(player2, "Kavu Titan").getFirst().getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Upkeep trigger damages creatures, kills a 1/1, and ignores noncreatures")
    void upkeepTriggerDamagesCreaturesButNotNoncreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new PlagueSpitter());
        Permanent kavuTitan = harness.addToBattlefieldAndReturn(player1, new KavuTitan());
        harness.addToBattlefield(player2, new Duskwalker());
        Permanent phyrexianLens = harness.addToBattlefieldAndReturn(player2, new PhyrexianLens());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(kavuTitan.getMarkedDamage()).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Duskwalker");
        harness.assertInGraveyard(player2, "Duskwalker");
        harness.assertOnBattlefield(player2, "Phyrexian Lens");
        assertThat(phyrexianLens.getMarkedDamage()).isZero();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Upkeep trigger does not fire on the opponent's upkeep")
    void upkeepTriggerDoesNotFireOnOpponentsUpkeep() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new PlagueSpitter());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Plague Spitter").getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
