package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.Cessation;
import com.github.laxika.magicalvibes.cards.e.EpharaEverSheltering;
import com.github.laxika.magicalvibes.cards.e.ErebosGodOfTheDead;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
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
        ErebosGodOfTheDead.class,
        Forest.class,
        GloriousAnthem.class
})
class InvasionOfTherosTest extends BaseCardTest {

    @Test
    void searchesForAnAuraGodOrDemigod() {
        Cessation aura = new Cessation();
        ErebosGodOfTheDead god = new ErebosGodOfTheDead();
        Card demigod = demigodCard();
        GloriousAnthem ordinaryEnchantment = new GloriousAnthem();
        castInvasion(List.of(ordinaryEnchantment, aura, god, demigod));

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(aura, god, demigod);
        assertThat(search.params().reveals()).isTrue();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

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
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(forest);
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ephara);
    }

    private void castInvasion(List<Card> deck) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(deck);
        harness.setHand(player1, List.of(new InvasionOfTheros()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addTransformedEphara() {
        harness.addToBattlefield(player1, new InvasionOfTheros());
        Permanent ephara = findPermanent("Invasion of Theros");
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

    private Card demigodCard() {
        Card card = new Card() {};
        card.setName("Test Demigod");
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.DEMIGOD));
        return card;
    }
}
