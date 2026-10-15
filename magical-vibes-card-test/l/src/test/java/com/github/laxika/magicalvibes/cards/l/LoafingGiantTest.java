package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KavuAggressor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoafingGiant.class, Forest.class, KavuAggressor.class, LeylineOfTheVoid.class})
class LoafingGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking mills a card and prevents the Giant's combat damage when it is a land")
    void attackingWithLandMilledPreventsDamage() {
        Permanent giant = addCreatureReady(player1, new LoafingGiant());
        harness.setLibrary(player1, List.of(new Forest()));
        int startingLife = gd.getLife(player2.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(giant.getId());
    }

    @Test
    @DisplayName("Attacking does not prevent combat damage when the milled card is not a land")
    void attackingWithNonlandMilledDealsDamage() {
        addCreatureReady(player1, new LoafingGiant());
        harness.setLibrary(player1, List.of(new KavuAggressor()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Blocking mills a card and prevents the Giant's combat damage when it is a land")
    void blockingWithLandMilledPreventsGiantDamage() {
        addCreatureReady(player1, new KavuAggressor());
        Permanent giant = addCreatureReady(player2, new LoafingGiant());
        harness.setLibrary(player2, List.of(new Forest()));

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveCombat();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(giant.getId());
    }

    @Test
    @DisplayName("Blocking mills a card and allows the Giant's combat damage when it is not a land")
    void blockingWithNonlandMilledDealsGiantDamage() {
        addCreatureReady(player1, new KavuAggressor());
        Permanent giant = addCreatureReady(player2, new LoafingGiant());
        harness.setLibrary(player2, List.of(new KavuAggressor()));

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveCombat();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(giant.getId());
        harness.assertInGraveyard(player1, "Kavu Aggressor");
    }

    @Test
    @DisplayName("An empty library does not prevent the Giant's combat damage")
    void emptyLibraryAllowsCombatDamage() {
        addCreatureReady(player1, new LoafingGiant());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveCombat();

        harness.assertLife(player2, 16);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only the top card is milled and a land below it does not prevent damage")
    void landBelowNonlandDoesNotPreventDamage() {
        addCreatureReady(player1, new LoafingGiant());
        KavuAggressor topCard = new KavuAggressor();
        Forest nextCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveCombat();

        harness.assertLife(player2, 16);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    @Test
    @DisplayName("A milled land exiled by Leyline still prevents combat damage")
    void landMilledIntoExilePreventsDamage() {
        addCreatureReady(player1, new LoafingGiant());
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
