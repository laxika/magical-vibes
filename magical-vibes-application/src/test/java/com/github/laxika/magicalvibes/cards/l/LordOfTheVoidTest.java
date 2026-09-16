package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LordOfTheVoid.class, Forest.class, GrizzlyBears.class})
class LordOfTheVoidTest extends BaseCardTest {

    @Test
    void exilesTopSevenAndStealsTheOnlyCreature() {
        addAttackingLord();
        GrizzlyBears creature = new GrizzlyBears();
        Card remainingCard = new Forest();
        Card drawnOnNextTurn = new Forest();
        List<Card> library = List.of(
                new Forest(), new Forest(), creature, new Forest(),
                new Forest(), new Forest(), new Forest(), drawnOnNextTurn, remainingCard);
        harness.setLibrary(player2, library);

        resolveCombatAndTrigger();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(
                        library.get(0).getId(), library.get(1).getId(), library.get(3).getId(),
                        library.get(4).getId(), library.get(5).getId(), library.get(6).getId());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingCard);
    }

    @Test
    void choosesOneCreatureWhenSeveralAreAmongTheExiledCards() {
        addAttackingLord();
        GrizzlyBears firstCreature = new GrizzlyBears();
        GrizzlyBears secondCreature = new GrizzlyBears();
        List<Card> library = List.of(firstCreature, new Forest(), secondCreature,
                new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player2, library);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(secondCreature.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(secondCreature.getId()))
                .hasSize(1);
        assertThat(gd.findExiledCard(firstCreature.getId())).isNotNull();
    }

    @Test
    void exilesCardsButStealsNothingWhenNoCreatureIsAmongTheTopSeven() {
        addAttackingLord();
        List<Card> library = List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest());
        harness.setLibrary(player2, library);

        resolveCombatAndTrigger();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrderElementsOf(library);
    }

    private Permanent addAttackingLord() {
        Permanent lord = addCreatureReady(player1, new LordOfTheVoid());
        lord.setAttacking(true);
        return lord;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
