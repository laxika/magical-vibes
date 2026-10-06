package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GolemsHeart;
import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SemblanceAnvil.class, CarapaceForger.class, GolemsHeart.class, Forest.class,
        IronMyr.class, SaberclawGolem.class, Shatter.class, Panharmonicon.class})
class SemblanceAnvilTest extends BaseCardTest {


    @Test
    @DisplayName("ETB triggers may ability to exile nonland card from hand")
    void etbTriggersImprintChoice() {
        harness.setHand(player1, List.of(new SemblanceAnvil(), new CarapaceForger()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // Resolve Anvil → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting imprint exiles nonland card from hand and imprints it")
    void acceptImprintExilesAndImprints() {
        harness.setHand(player1, List.of(new SemblanceAnvil(), new CarapaceForger()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // Resolve Anvil → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt

        // Accept the may ability — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();

        // Should be awaiting card choice from hand
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ImprintFromHandChoice.class);

        // Choose the creature (index 0 in remaining hand)
        harness.handleCardChosen(player1, 0);

        // Carapace Forger should be exiled
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Carapace Forger"));

        // Carapace Forger should no longer be in hand
        harness.assertNotInHand(player1, "Carapace Forger");

        // Anvil should have Carapace Forger imprinted
        Permanent anvil = findPermanent(player1, "Semblance Anvil");
        assertThat(gd.getImprintedCard(anvil.getCard())).isNotNull();
        assertThat(gd.getImprintedCard(anvil.getCard()).getName()).isEqualTo("Carapace Forger");
    }

    @Test
    @DisplayName("Declining imprint leaves card in hand")
    void declineImprintLeavesCardInHand() {
        harness.setHand(player1, List.of(new SemblanceAnvil(), new CarapaceForger()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // Resolve Anvil → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();

        // Carapace Forger should still be in hand
        harness.assertInHand(player1, "Carapace Forger");

        // Anvil should have nothing imprinted
        Permanent anvil = findPermanent(player1, "Semblance Anvil");
        assertThat(gd.getImprintedCard(anvil.getCard())).isNull();
    }

    @Test
    @DisplayName("Only land cards in hand skips imprint gracefully")
    void onlyLandsInHandSkips() {
        harness.setHand(player1, List.of(new SemblanceAnvil(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // Resolve Anvil → ETB MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt

        // Accept may — inner effect resolves inline, but no nonland cards → skip
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();

        // Forest should still be in hand
        harness.assertInHand(player1, "Forest");

        // Anvil should have nothing imprinted
        Permanent anvil = findPermanent(player1, "Semblance Anvil");
        assertThat(gd.getImprintedCard(anvil.getCard())).isNull();
    }


    @Test
    @DisplayName("Creature spells cost {2} less when a creature is imprinted")
    void creatureSpellsCostLessWithCreatureImprinted() {
        // Set up Anvil with a creature imprinted
        SemblanceAnvil anvilCard = new SemblanceAnvil();
        CarapaceForger imprintedForger = new CarapaceForger();
        harness.setExile(player1, List.of(imprintedForger));
        gd.setImprintedCard(anvilCard, imprintedForger);
        harness.addToBattlefield(player1, anvilCard);

        // Carapace Forger costs {1}{G}. With {2} reduction, only needs {G}.
        harness.setHand(player1, List.of(new CarapaceForger()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getCard().getName()).isEqualTo("Carapace Forger");
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Artifact spells are not reduced when only a creature is imprinted")
    void artifactSpellsNotReducedWithCreatureImprinted() {
        // Set up Anvil with a creature imprinted
        SemblanceAnvil anvilCard = new SemblanceAnvil();
        CarapaceForger imprintedForger = new CarapaceForger();
        harness.setExile(player1, List.of(imprintedForger));
        gd.setImprintedCard(anvilCard, imprintedForger);
        harness.addToBattlefield(player1, anvilCard);

        // Golem's Heart costs {2}. No reduction (artifact ≠ creature).
        harness.setHand(player1, List.of(new GolemsHeart()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // 1 mana is not enough (needs full 2)
        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Artifact spells cost {2} less when an artifact is imprinted")
    void artifactSpellsCostLessWithArtifactImprinted() {
        // Set up Anvil with an artifact imprinted
        SemblanceAnvil anvilCard = new SemblanceAnvil();
        GolemsHeart imprintedHeart = new GolemsHeart();
        harness.setExile(player1, List.of(imprintedHeart));
        gd.setImprintedCard(anvilCard, imprintedHeart);
        harness.addToBattlefield(player1, anvilCard);

        // Golem's Heart costs {2}. With {2} reduction, it's free.
        harness.setHand(player1, List.of(new GolemsHeart()));

        harness.castArtifact(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getCard().getName()).isEqualTo("Golem's Heart");
    }

    @Test
    @DisplayName("No cost reduction without an imprinted card")
    void noCostReductionWithoutImprint() {
        // Set up Anvil with nothing imprinted
        harness.addToBattlefield(player1, new SemblanceAnvil());

        // Carapace Forger costs {1}{G}. No reduction (nothing imprinted).
        harness.setHand(player1, List.of(new CarapaceForger()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        // 1 green is not enough (needs {1}{G} = 2 total)
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Two Semblance Anvils stack cost reduction to {4}")
    void twoAnvilsStackReduction() {
        // Set up two Anvils, both with creatures imprinted
        SemblanceAnvil anvil1 = new SemblanceAnvil();
        CarapaceForger firstImprint = new CarapaceForger();
        harness.setExile(player1, List.of(firstImprint));
        gd.setImprintedCard(anvil1, firstImprint);
        harness.addToBattlefield(player1, anvil1);

        SemblanceAnvil anvil2 = new SemblanceAnvil();
        CarapaceForger secondImprint = new CarapaceForger();
        harness.setExile(player1, List.of(secondImprint));
        gd.setImprintedCard(anvil2, secondImprint);
        harness.addToBattlefield(player1, anvil2);

        // Carapace Forger costs {1}{G}. With {4} total reduction, only needs {G}.
        // (reduction of 4 on 1 generic = 0 generic, so just colored {G})
        harness.setHand(player1, List.of(new CarapaceForger()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getCard().getName()).isEqualTo("Carapace Forger");
    }

    @Test
    @DisplayName("Cost reduction does not affect opponent's spells")
    void costReductionDoesNotAffectOpponent() {
        // Set up Anvil under player1's control with a creature imprinted
        SemblanceAnvil anvilCard = new SemblanceAnvil();
        CarapaceForger imprintedForger = new CarapaceForger();
        harness.setExile(player1, List.of(imprintedForger));
        gd.setImprintedCard(anvilCard, imprintedForger);
        harness.addToBattlefield(player1, anvilCard);

        // Player 2 tries to cast a creature — should not get reduction
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CarapaceForger()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        // 1 green is not enough for player2 (no reduction from player1's Anvil)
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Imprint still exiles a card when Anvil leaves before its trigger resolves")
    void imprintResolvesAfterSourceLeaves() {
        harness.setHand(player1, List.of(new SemblanceAnvil(), new CarapaceForger(), new Shatter()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 1, harness.getPermanentId(player1, "Semblance Anvil"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ImprintFromHandChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.assertNotInHand(player1, "Carapace Forger");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Carapace Forger"));
    }

    @Test
    @DisplayName("Reduction stops when the imprinted card leaves exile")
    void noReductionAfterImprintedCardLeavesExile() {
        harness.setHand(player1, List.of(new SemblanceAnvil(), new CarapaceForger()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        var imprinted = gd.getPlayerExiledCards(player1.getId()).getFirst();
        gd.removeFromExile(imprinted.getId());
        gd.addCardToHand(player1.getId(), imprinted);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Both cards exiled by doubled imprint triggers contribute their card types")
    void doubledImprintRetainsBothCards() {
        harness.addToBattlefield(player1, new Panharmonicon());
        harness.setHand(player1, List.of(new SemblanceAnvil(), new CarapaceForger(), new GolemsHeart()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);

        harness.setHand(player1, List.of(new CarapaceForger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An artifact creature imprint reduces both creature and artifact spells")
    void artifactCreatureImprintReducesEitherType() {
        var anvil = new SemblanceAnvil();
        harness.addToBattlefield(player1, anvil);
        var myr = new IronMyr();
        harness.setExile(player1, List.of(myr));
        gd.setImprintedCard(anvil, myr);
        harness.setHand(player1, List.of(new CarapaceForger(), new GolemsHeart()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Sharing two card types grants only one reduction")
    void sharedMultipleTypesReduceCostOnlyOnce() {
        var anvil = new SemblanceAnvil();
        harness.addToBattlefield(player1, anvil);
        var myr = new IronMyr();
        harness.setExile(player1, List.of(myr));
        gd.setImprintedCard(anvil, myr);
        harness.setHand(player1, List.of(new SaberclawGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Instant spells receive the discount without reducing colored mana")
    void instantImprintReducesOnlyGenericMana() {
        var anvil = new SemblanceAnvil();
        harness.addToBattlefield(player1, anvil);
        var imprint = new Shatter();
        harness.setExile(player1, List.of(imprint));
        gd.setImprintedCard(anvil, imprint);
        var target = harness.addToBattlefieldAndReturn(player2, new GolemsHeart());
        harness.setHand(player1, List.of(new Shatter()));
        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Two Anvils reduce a five-mana spell to one mana")
    void twoAnvilsApplyFullCombinedReduction() {
        var myr = new IronMyr();
        var forger = new CarapaceForger();
        harness.setExile(player1, List.of(myr, forger));
        var first = new SemblanceAnvil();
        var second = new SemblanceAnvil();
        harness.addToBattlefield(player1, first);
        harness.addToBattlefield(player1, second);
        gd.setImprintedCard(first, myr);
        gd.setImprintedCard(second, forger);
        harness.setHand(player1, List.of(new SaberclawGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
