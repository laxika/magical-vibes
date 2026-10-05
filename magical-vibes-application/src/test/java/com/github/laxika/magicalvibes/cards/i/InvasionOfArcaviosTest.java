package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Divination;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Divination.class, InvasionOfArcavios.class, InvocationOfTheFounders.class,
        Shock.class})
class InvasionOfArcaviosTest extends BaseCardTest {

    @Test
    @DisplayName("The Siege searches the library, graveyard, and sideboard for an instant or sorcery")
    void searchesAllAllowedZones() {
        Card libraryCard = new Shock();
        Card graveyardCard = new Divination();
        Card sideboardCard = new Shock();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(sideboardCard)));

        castInvasion();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.pool()).containsExactlyInAnyOrder(libraryCard, graveyardCard, sideboardCard);
        assertThat(choice.libraryCardIds()).containsExactly(libraryCard.getId());
        assertThat(choice.outsideGameCardIds()).containsExactly(sideboardCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(sideboardCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(sideboardCard);
        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
    }

    @Test
    @DisplayName("Defeating the Siege exiles it and casts Invocation of the Founders transformed")
    void defeatCastsBackFace() {
        gd.playerDecks.get(player1.getId()).clear();
        castInvasion();

        Permanent battle = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Invasion of Arcavios".equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent backFace = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Invocation of the Founders".equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
        assertThat(backFace.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Invocation of the Founders may copy an instant cast from hand")
    void backFaceCopiesInstantFromHand() {
        addInvocation();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.stack).extracting(entry -> entry.getCard().getName())
                .contains("Shock");
    }

    @Test
    void returnsInstantFromLibraryAndExcludesBattles() {
        Card instant = new Shock();
        Card battle = new InvasionOfArcavios();
        harness.setLibrary(player1, List.of(instant, battle));
        harness.setGraveyard(player1, List.of());
        castInvasion();

        assertThat(gd.interaction.activeInteraction(
                PendingInteraction.SearchLibraryAndOrGraveyardChoice.class).pool())
                .containsExactly(instant);
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instant);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(battle);
    }

    @Test
    void returnsSorceryFromGraveyard() {
        Card sorcery = new Divination();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(sorcery));
        castInvasion();

        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void mayFailToFindAnInstantInLibrary() {
        Card instant = new Shock();
        harness.setLibrary(player1, List.of(instant));
        harness.setGraveyard(player1, List.of());
        castInvasion();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instant);
    }

    @Test
    void copiedInstantAndOriginalBothDealDamage() {
        addInvocation();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Shock).hasSize(1);
    }

    @Test
    void mayChooseADifferentPlayerForCopy() {
        addInvocation();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void decliningCopyLeavesOnlyOriginalDamage() {
        addInvocation();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copiesSorceryFromHandWithoutTriggeringAgainForCopy() {
        addInvocation();
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.castFromHand(player1, new Divination(), "{2}{U}");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void opponentsInstantDoesNotTriggerInvocation() {
        addInvocation();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void mayDeclineCastingDefeatedSiege() {
        harness.setLibrary(player1, List.of());
        castInvasion();
        Permanent battle = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Invasion of Arcavios"));
        Card front = battle.getOriginalCard();
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        harness.assertNotOnBattlefield(player1, "Invocation of the Founders");
        assertThat(gd.findExiledCard(front.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingBattleDoesNotTriggerInvocation() {
        addInvocation();

        harness.castFromHand(player1, new InvasionOfArcavios(), "{3}{U}{U}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void instantCastFromLibraryDoesNotTriggerInvocation() {
        addInvocation();
        Card instant = new Shock();
        harness.setLibrary(player1, List.of(instant));
        gd.libraryTopCardFreePlayPermissionsUntilEndOfTurn.put(player1.getId(), instant.getId());
        harness.setLife(player2, 20);

        harness.castFromLibraryTop(player1, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    private void addInvocation() {
        InvasionOfArcavios front = new InvasionOfArcavios();
        Permanent invocation = harness.addToBattlefieldAndReturn(player1, front);
        invocation.setCard(front.getBackFaceCard());
        invocation.setTransformed(true);
    }

    private void castInvasion() {
        harness.castFromHand(player1, new InvasionOfArcavios(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
