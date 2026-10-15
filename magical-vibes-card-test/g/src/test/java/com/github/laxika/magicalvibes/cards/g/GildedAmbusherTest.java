package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GildedAmbusher.class, GrizzlyBears.class, Shock.class, Forest.class, Clone.class})
class GildedAmbusherTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice on attack exiles an opposing permanent and random nonland card, then deals their total mana value")
    void sacrificesAndExilesBeforeDealingDamage() {
        Permanent ambusher = addCreatureReady(player1, new GildedAmbusher());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());
        Forest forest = new Forest();
        Shock exiledFromLibrary = new Shock();
        harness.setLibrary(player2, List.of(forest, exiledFromLibrary));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ambusher).doesNotContain(sacrifice);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposing);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(exiledFromLibrary);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest);
        // Three from the exile trigger and four from Ambusher's combat damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Declining the attack trigger does nothing")
    void mayBeDeclined() {
        Permanent ambusher = addCreatureReady(player1, new GildedAmbusher());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Shock()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ambusher, sacrifice);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposing);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent without an eligible permanent still exiles a random nonland library card")
    void exilesLibraryCardWithoutOpposingPermanent() {
        addCreatureReady(player1, new GildedAmbusher());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Forest forest = new Forest();
        Shock shock = new Shock();
        harness.addToBattlefield(player2, forest);
        harness.setLibrary(player2, List.of(new Forest(), shock));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, sacrifice.getId());
            resolveAllTriggers();
        });

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(shock);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getCard).contains(forest);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A library containing only lands does not prevent permanent exile and damage")
    void exilesPermanentWithOnlyLandsInLibrary() {
        addCreatureReady(player1, new GildedAmbusher());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(forest));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, sacrifice.getId());
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposing);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opposing.getOriginalCard());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The opponent chooses which nonland nontoken permanent to exile")
    void opponentChoosesPermanent() {
        addCreatureReady(player1, new GildedAmbusher());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GildedAmbusher());
        harness.setLibrary(player2, List.of());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, sacrifice.getId());
            resolveAllTriggers();

            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                    .isEqualTo(player2.getId());
            harness.handlePermanentChosen(player2, second.getId());
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(second.getOriginalCard());
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Damage uses the exiled Clone card's mana value after its copy effect ends")
    void usesExiledCardsManaValueRatherThanCopiedPermanent() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Clone clone = new Clone();
        harness.castFromHand(player1, clone, "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        addCreatureReady(player2, new GildedAmbusher());
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(1));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player2, true);
            harness.handlePermanentChosen(player2, bears.getId());
            resolveAllTriggers();
        });

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(clone, shock);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 15);
    }
}
