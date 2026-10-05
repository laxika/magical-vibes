package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.Godsire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GuildpactParagon;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KitchenFinks;
import com.github.laxika.magicalvibes.cards.o.OonaQueenOfTheFae;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        Godsire.class,
        GrizzlyBears.class,
        GuildpactParagon.class,
        InvasionOfRavnica.class,
        Island.class,
        KitchenFinks.class,
        OonaQueenOfTheFae.class,
        Plains.class,
        Shock.class
})
class InvasionOfRavnicaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters targeting an opponent's nonland permanent that is not exactly two colors")
    void etbExilesNonTwoColorPermanent() {
        Permanent exactlyTwoColors = harness.addToBattlefieldAndReturn(player2, new OonaQueenOfTheFae());
        Permanent threeColors = harness.addToBattlefieldAndReturn(player2, new Godsire());
        Card invasion = new InvasionOfRavnica();

        harness.setHand(player1, List.of(invasion));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        var targets = harness.getValidTargetService().computeValidTargetsForSpell(
                gd, invasion, player1.getId(), null);
        assertThat(targets.validPermanentIds()).contains(threeColors.getId())
                .doesNotContain(exactlyTwoColors.getId());

        gs.playCard(gd, player1, 0, 0, threeColors.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(exactlyTwoColors)
                .noneMatch(permanent -> permanent.getId().equals(threeColors.getId()));
    }

    @Test
    @DisplayName("Defeat exiles the Siege and casts Guildpact Paragon transformed")
    void defeatCastsBackFace() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Godsire());
        castInvasion(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent battle = findPermanent(player1, "Invasion of Ravnica");
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent paragon = findPermanent(player1, "Guildpact Paragon");
        assertThat(paragon.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Guildpact Paragon triggers only for exactly two-color spells and cards")
    void backFaceTriggersForExactlyTwoColors() {
        Permanent paragon = harness.addToBattlefieldAndReturn(player1, new GuildpactParagon());
        paragon.setTransformed(true);
        Card exactlyTwoColors = new OonaQueenOfTheFae();
        Card threeColors = new Godsire();
        harness.setLibrary(player1, List.of(exactlyTwoColors, threeColors, new GrizzlyBears(),
                new Island(), new Shock(), new Plains()));

        harness.setHand(player1, List.of(new KitchenFinks()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(exactlyTwoColors.getId())
                .doesNotContain(threeColors.getId());

        harness.handleMultipleCardsChosen(player1, List.of(exactlyTwoColors.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(exactlyTwoColors);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void etbAllowsColorlessButRejectsLandsAndOwnPermanents() {
        Permanent colorless = harness.addToBattlefieldAndReturn(player2, new GuildpactParagon());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card invasion = new InvasionOfRavnica();
        harness.setHand(player1, List.of(invasion));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        var targets = harness.getValidTargetService().computeValidTargetsForSpell(
                gd, invasion, player1.getId(), null);
        assertThat(targets.validPermanentIds()).contains(colorless.getId())
                .doesNotContain(land.getId(), own.getId());

        gs.playCard(gd, player1, 0, 0, colorless.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(colorless.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land).doesNotContain(colorless);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(own);
    }

    @Test
    void backFaceMayDeclineAndOnlyLooksAtSixCards() {
        harness.addToBattlefield(player1, new GuildpactParagon());
        Card first = new OonaQueenOfTheFae();
        Card second = new KitchenFinks();
        List<Card> topSix = List.of(first, second, new Godsire(), new GrizzlyBears(),
                new Island(), new Shock());
        Card seventh = new OonaQueenOfTheFae();
        harness.setLibrary(player1, List.of(topSix.get(0), topSix.get(1), topSix.get(2),
                topSix.get(3), topSix.get(4), topSix.get(5), seventh));
        castTwoColorSpell();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(seventh);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 7))
                .containsExactlyInAnyOrderElementsOf(topSix);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void backFaceCanSelectFromShortLibrary() {
        harness.addToBattlefield(player1, new GuildpactParagon());
        Card matching = new OonaQueenOfTheFae();
        Card other = new Island();
        harness.setLibrary(player1, List.of(matching, other));
        castTwoColorSpell();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(matching.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matching);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void backFaceWithNoMatchingCardsReturnsAllCardsToLibrary() {
        harness.addToBattlefield(player1, new GuildpactParagon());
        List<Card> cards = List.of(new Godsire(), new GrizzlyBears(), new Island());
        harness.setLibrary(player1, cards);
        castTwoColorSpell();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(cards);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void backFaceDoesNotTriggerForMonocolorSpell() {
        harness.addToBattlefield(player1, new GuildpactParagon());
        Card matching = new OonaQueenOfTheFae();
        harness.setLibrary(player1, List.of(matching));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(matching);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void backFaceDoesNotTriggerForThreeColorSpell() {
        harness.addToBattlefield(player1, new GuildpactParagon());
        harness.setHand(player1, List.of(new Godsire()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void backFaceDoesNotTriggerForOpponentsTwoColorSpell() {
        harness.addToBattlefield(player2, new GuildpactParagon());
        castTwoColorSpell();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castTwoColorSpell() {
        harness.setHand(player1, List.of(new KitchenFinks()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
    }

    private void castInvasion(UUID targetId) {
        harness.setHand(player1, List.of(new InvasionOfRavnica()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }

}
