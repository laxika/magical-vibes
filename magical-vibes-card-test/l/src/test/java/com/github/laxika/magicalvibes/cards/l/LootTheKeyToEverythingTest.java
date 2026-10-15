package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AncientDen;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LootTheKeyToEverything.class, AncientDen.class, Forest.class, GrizzlyBears.class,
        RestInPeace.class, SolRing.class, LightningBolt.class})
class LootTheKeyToEverythingTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles one card per distinct type among other nonland permanents")
    void exilesPerDistinctOtherNonlandPermanentType() {
        harness.addToBattlefield(player1, new LootTheKeyToEverything());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SolRing());
        harness.addToBattlefield(player1, new RestInPeace());
        List<Card> library = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, library);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyElementsOf(library.subList(0, 3));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(3));
        assertThat(gd.exilePlayPermissions).containsKeys(
                library.get(0).getId(), library.get(1).getId(), library.get(2).getId());
    }

    @Test
    @DisplayName("Excludes Loot itself and all lands, including artifact lands")
    void excludesSourceAndLands() {
        harness.addToBattlefield(player1, new LootTheKeyToEverything());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new AncientDen());
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    @DisplayName("Counts permanents when the upkeep ability resolves")
    void countsAtResolution() {
        harness.addToBattlefield(player1, new LootTheKeyToEverything());
        List<Card> library = List.of(new Forest(), new Forest());
        harness.setLibrary(player1, library);

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new SolRing());
        harness.addToBattlefield(player1, new RestInPeace());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(library);
    }
    @Test
    void wardCountersOpponentSpellWhenTheyCannotPay() {
        var loot = harness.addToBattlefieldAndReturn(player1, new LootTheKeyToEverything());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, loot.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Loot, the Key to Everything");
        harness.assertInGraveyard(player2, "Lightning Bolt");
    }

    @Test
    void duplicateTypesAndOpposingPermanentsDoNotIncreaseCount() {
        harness.addToBattlefield(player1, new LootTheKeyToEverything());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SolRing());
        harness.addToBattlefield(player2, new RestInPeace());
        List<Card> library = List.of(new Forest(), new Forest());
        harness.setLibrary(player1, library);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(library.get(0));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(1));
    }

    @Test
    void exilesOnlyAvailableCardsWhenLibraryIsShort() {
        harness.addToBattlefield(player1, new LootTheKeyToEverything());
        harness.addToBattlefield(player1, new SolRing());
        harness.addToBattlefield(player1, new RestInPeace());
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new LootTheKeyToEverything());
        harness.addToBattlefield(player1, new SolRing());
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void mayPlayExiledLandDuringMainPhase() {
        harness.addToBattlefield(player1, new LootTheKeyToEverything());
        harness.addToBattlefield(player1, new SolRing());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(land);
    }

    @Test
    void castingExiledSpellStillRequiresMana() {
        harness.addToBattlefield(player1, new LootTheKeyToEverything());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card spell = new SolRing();
        harness.setLibrary(player1, List.of(spell));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sol Ring");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
    }
}
