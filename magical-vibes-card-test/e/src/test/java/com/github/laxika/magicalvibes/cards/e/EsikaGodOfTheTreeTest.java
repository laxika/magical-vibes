package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzledOutrider;
import com.github.laxika.magicalvibes.cards.h.HalvarGodOfBattle;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.t.ThePrismaticBridge;
import com.github.laxika.magicalvibes.cards.t.TyvarKell;
import com.github.laxika.magicalvibes.cards.v.VorinclexMonstrousRaider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EsikaGodOfTheTree.class, GrizzledOutrider.class, HalvarGodOfBattle.class,
        ThePrismaticBridge.class, SnowCoveredForest.class,
        TyvarKell.class, VorinclexMonstrousRaider.class})
class EsikaGodOfTheTreeTest extends BaseCardTest {

    @Test
    void frontFaceGrantsOtherLegendaryCreaturesVigilanceAndAnyColorMana() {
        EsikaGodOfTheTree card = new EsikaGodOfTheTree();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent halvar = addCreatureReady(player1, new HalvarGodOfBattle());
        Permanent nonlegendary = harness.addToBattlefieldAndReturn(player1, new GrizzledOutrider());

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, halvar, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonlegendary, Keyword.VIGILANCE)).isFalse();

        int halvarIndex = gd.playerBattlefields.get(player1.getId()).indexOf(halvar);
        harness.activateAbility(player1, halvarIndex, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(halvar.isTapped()).isTrue();
    }

    @Test
    void prismaticBridgePutsTheFirstCreatureOrPlaneswalkerOntoTheBattlefield() {
        harness.addToBattlefield(player1, new ThePrismaticBridge());
        Card nonmatch = new SnowCoveredForest();
        Card planeswalker = new TyvarKell();
        harness.setLibrary(player1, List.of(nonmatch, planeswalker));

        harness.forceActivePlayer(player1);
        gd.turnNumber = 1;
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        resolveUpkeep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getId().equals(planeswalker.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(nonmatch);
    }

    @Test
    void esikaCanTapForManaOfAnyColorWithoutUsingTheStack() {
        Permanent esika = addCreatureReady(player1, new EsikaGodOfTheTree());

        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            esika.untap();
            harness.activateAbility(player1, 0, null, null);
            harness.handleListChoice(player1, color.name());

            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
            assertThat(esika.isTapped()).isTrue();
            assertThat(gd.stack).isEmpty();
        }
    }

    @Test
    void newlyEnteredLegendaryCreatureCannotUseGrantedTapAbility() {
        harness.addToBattlefield(player1, new EsikaGodOfTheTree());
        harness.addToBattlefield(player1, new HalvarGodOfBattle());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void grantsDoNotApplyToOpponentsLegendaryCreaturesOrLegendaryNoncreatures() {
        harness.addToBattlefield(player1, new EsikaGodOfTheTree());
        Permanent opposingHalvar = addCreatureReady(player2, new HalvarGodOfBattle());
        Permanent bridge = harness.addToBattlefieldAndReturn(player1, new ThePrismaticBridge());

        assertThat(gqs.hasKeyword(gd, opposingHalvar, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bridge, Keyword.VIGILANCE)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nonlegendaryCreatureDoesNotReceiveTheManaAbility() {
        harness.addToBattlefield(player1, new EsikaGodOfTheTree());
        addCreatureReady(player1, new GrizzledOutrider());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void castingBackFaceUsesBridgeAbilityAndDoesNotGrantEsikasAbilities() {
        harness.setHand(player1, List.of(new EsikaGodOfTheTree()));
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            harness.addMana(player1, color, 1);
        }
        Permanent halvar = addCreatureReady(player1, new HalvarGodOfBattle());
        Card creature = new EsikaGodOfTheTree();
        harness.setLibrary(player1, List.of(creature));

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Prismatic Bridge");
        assertThat(gqs.hasKeyword(gd, halvar, Keyword.VIGILANCE)).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);

        harness.forceStep(TurnStep.UNTAP);
        resolveUpkeep();

        harness.assertOnBattlefield(player1, "Esika, God of the Tree");
        assertThat(gqs.hasKeyword(gd, halvar, Keyword.VIGILANCE)).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void bridgeStopsAtFirstCreatureAndPutsOnlyRevealedNonmatchesBelowUnrevealedCards() {
        harness.addToBattlefield(player1, new ThePrismaticBridge());
        Card firstLand = new SnowCoveredForest();
        Card secondLand = new SnowCoveredForest();
        Card creature = new HalvarGodOfBattle();
        Card unrevealed = new TyvarKell();
        harness.setLibrary(player1, List.of(firstLand, secondLand, creature, unrevealed));
        harness.forceStep(TurnStep.UNTAP);

        resolveUpkeep();

        harness.assertOnBattlefield(player1, "Halvar, God of Battle");
        harness.assertNotOnBattlefield(player1, "Sword of the Realms");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unrevealed);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(firstLand, secondLand);
        assertThat(findPermanent(player1, "Halvar, God of Battle").isTapped()).isFalse();
    }

    @Test
    void bridgeDoesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new ThePrismaticBridge());
        Card creature = new HalvarGodOfBattle();
        harness.setLibrary(player1, List.of(creature));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        harness.assertNotOnBattlefield(player1, "Halvar, God of Battle");
    }

    @Test
    void bridgeRetainsEntireLibraryWhenThereIsNoMatchingCard() {
        harness.addToBattlefield(player1, new ThePrismaticBridge());
        Card firstLand = new SnowCoveredForest();
        Card secondLand = new SnowCoveredForest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));
        harness.forceStep(TurnStep.UNTAP);

        resolveUpkeep();

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstLand, secondLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void bridgeHandlesAnEmptyLibraryWithoutPuttingAnythingOntoBattlefield() {
        harness.addToBattlefield(player1, new ThePrismaticBridge());
        harness.setLibrary(player1, List.of());
        harness.forceStep(TurnStep.UNTAP);

        resolveUpkeep();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bridgePreservesReplacementEffectsOnPlaneswalkerStartingLoyalty() {
        harness.addToBattlefield(player1, new ThePrismaticBridge());
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        harness.setLibrary(player1, List.of(new TyvarKell()));
        harness.forceStep(TurnStep.UNTAP);

        resolveUpkeep();

        assertThat(findPermanent(player1, "Tyvar Kell").getCounterCount(CounterType.LOYALTY))
                .isEqualTo(6);
    }

    private void resolveUpkeep() {
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            harness.passUntil(TurnStep.UPKEEP);
            harness.passBothPriorities();
        });
    }
}
