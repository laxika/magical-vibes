package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void mustChooseACreatureWhenSeveralAreExiled() {
        addAttackingLord();
        GrizzlyBears firstCreature = new GrizzlyBears();
        GrizzlyBears secondCreature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(firstCreature, secondCreature,
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            resolveCombat();
            resolveAllTriggers();

            assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                    .isInstanceOf(IllegalStateException.class);
            harness.handleMultipleCardsChosen(player1, List.of(firstCreature.getId()));
        });

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(firstCreature.getId())).isNull();
        assertThat(gd.findExiledCard(secondCreature.getId())).isNotNull();
    }

    @Test
    void cannotChooseANoncreatureAmongTheExiledCards() {
        addAttackingLord();
        GrizzlyBears firstCreature = new GrizzlyBears();
        GrizzlyBears secondCreature = new GrizzlyBears();
        Forest land = new Forest();
        harness.setLibrary(player2, List.of(firstCreature, secondCreature, land,
                new Forest(), new Forest(), new Forest(), new Forest()));

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            resolveCombat();
            resolveAllTriggers();

            assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(land.getId())))
                    .isInstanceOf(IllegalStateException.class);
            harness.handleMultipleCardsChosen(player1, List.of(secondCreature.getId()));
        });

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        assertThat(gd.findExiledCard(firstCreature.getId())).isNotNull();
        assertThat(gd.findExiledCard(secondCreature.getId())).isNull();
    }

    @Test
    void exilesTheWholeShortLibraryAndStillPutsItsCreatureOntoTheBattlefield() {
        addAttackingLord();
        GrizzlyBears creature = new GrizzlyBears();
        Forest land = new Forest();
        harness.setLibrary(player2, List.of(land, creature));

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            resolveCombat();
            resolveAllTriggers();
        });

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land);
        assertThat(gd.findExiledCard(creature.getId())).isNull();
    }

    @Test
    void triggerResolvesAfterLordLeavesTheBattlefield() {
        Permanent lord = addAttackingLord();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(creature,
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest()));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(lord);
        gd.playerGraveyards.get(player1.getId()).add(lord.getCard());

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Lord of the Void");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(creature.getId())).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(6);
    }

    private Permanent addAttackingLord() {
        Permanent lord = addCreatureReady(player1, new LordOfTheVoid());
        lord.setAttacking(true);
        return lord;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
