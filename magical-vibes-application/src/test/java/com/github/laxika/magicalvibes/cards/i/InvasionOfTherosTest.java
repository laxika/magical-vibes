package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.Cessation;
import com.github.laxika.magicalvibes.cards.d.DaxosBlessedByTheSun;
import com.github.laxika.magicalvibes.cards.e.EpharaEverSheltering;
import com.github.laxika.magicalvibes.cards.e.ErebosGodOfTheDead;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GitaxianSpellstalker;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.k.KhenraSpellspear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        InvasionOfTheros.class,
        EpharaEverSheltering.class,
        Cessation.class,
        DaxosBlessedByTheSun.class,
        ErebosGodOfTheDead.class,
        Forest.class,
        GloriousAnthem.class,
        KhenraSpellspear.class,
        GitaxianSpellstalker.class
})
class InvasionOfTherosTest extends BaseCardTest {

    @Test
    void searchesForAnAuraGodOrDemigod() {
        Cessation aura = new Cessation();
        ErebosGodOfTheDead god = new ErebosGodOfTheDead();
        Card demigod = new DaxosBlessedByTheSun();
        GloriousAnthem ordinaryEnchantment = new GloriousAnthem();
        castInvasion(List.of(ordinaryEnchantment, aura, god, demigod));

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(aura, god, demigod);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(aura);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    void defeatingTheSiegeCastsEpharaTransformed() {
        castInvasion(List.of());

        Permanent invasion = findPermanent("Invasion of Theros");
        invasion.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, invasion));

        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }
        harness.passBothPriorities();

        Permanent ephara = findPermanent("Ephara, Ever-Sheltering");
        assertThat(ephara.isTransformed()).isTrue();
    }

    @Test
    void epharaGainsLifelinkAndIndestructibleWithThreeOtherEnchantments() {
        Permanent ephara = addTransformedEphara();

        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GloriousAnthem());
        assertThat(gqs.hasKeyword(gd, ephara, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, ephara, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.addToBattlefield(player1, new GloriousAnthem());
        assertThat(gqs.hasKeyword(gd, ephara, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, ephara, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void epharaDrawsWhenAnotherEnchantmentEntersUnderYourControl() {
        Permanent ephara = addTransformedEphara();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ephara);
    }

    @Test
    void canChooseAGodForTheSearch() {
        ErebosGodOfTheDead god = new ErebosGodOfTheDead();
        castInvasion(List.of(god));
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(god);
    }

    @Test
    void canChooseADemigodForTheSearch() {
        DaxosBlessedByTheSun demigod = new DaxosBlessedByTheSun();
        castInvasion(List.of(demigod));
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(demigod);
    }

    @Test
    void canFailToFindEvenWithAnEligibleCardInTheLibrary() {
        Cessation aura = new Cessation();
        castInvasion(List.of(aura));
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void searchWithNoEligibleCardsLeavesThemInTheLibrary() {
        Forest forest = new Forest();
        GloriousAnthem enchantment = new GloriousAnthem();
        castInvasion(List.of(forest, enchantment));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, enchantment);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void controllerMayDeclineToCastTheDefeatedSiege() {
        castInvasion(List.of());
        Permanent invasion = findPermanent("Invasion of Theros");
        invasion.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, invasion));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(invasion.getCard().getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Invasion of Theros");
        harness.assertNotOnBattlefield(player1, "Ephara, Ever-Sheltering");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void epharaDoesNotCountOpposingEnchantmentsAndLosesKeywordsBelowTheThreshold() {
        Permanent ephara = addTransformedEphara();
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new GloriousAnthem());
        assertThat(gqs.hasKeyword(gd, ephara, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, ephara, Keyword.INDESTRUCTIBLE)).isFalse();

        Permanent third = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        assertThat(gqs.hasKeyword(gd, ephara, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, ephara, Keyword.INDESTRUCTIBLE)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(third);
        assertThat(gqs.hasKeyword(gd, ephara, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, ephara, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void castingEpharaTransformedDoesNotTriggerProwess() {
        castInvasion(List.of());
        harness.addToBattlefield(player1, new KhenraSpellspear());
        Permanent invasion = findPermanent("Invasion of Theros");
        invasion.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, invasion));
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ephara, Ever-Sheltering");
    }

    @Test
    void epharaDoesNotDrawForAnOpponentsEnchantmentOrANonEnchantment() {
        addTransformedEphara();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.enterBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void epharaDoesNotTriggerForItsOwnEntry() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.enterBattlefieldAndReturn(player1, new EpharaEverSheltering());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castInvasion(List<Card> deck) {
        harness.setLibrary(player1, deck);
        harness.castFromHand(player1, new InvasionOfTheros(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addTransformedEphara() {
        Permanent ephara = harness.addToBattlefieldAndReturn(player1, new InvasionOfTheros());
        ephara.setCard(ephara.getCard().getBackFaceCard());
        ephara.setTransformed(true);
        return ephara;
    }

    private Permanent findPermanent(String name) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }

}
