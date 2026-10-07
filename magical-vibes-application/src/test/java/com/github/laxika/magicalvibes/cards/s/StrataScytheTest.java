package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PalladiumMyr;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrataScythe.class, PalladiumMyr.class, Plains.class, Forest.class, Mountain.class})
class StrataScytheTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches to a creature you control after paying three mana")
    void equipAttachesToControlledCreature() {
        Permanent creature = addCreatureReady(player1, new PalladiumMyr());
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new StrataScythe());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, creature.getId());
        assertThat(scythe.getAttachedTo()).isNull();
        harness.passBothPriorities();

        assertThat(scythe.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip cannot be activated with only two mana")
    void equipRequiresThreeMana() {
        Permanent creature = addCreatureReady(player1, new PalladiumMyr());
        harness.addToBattlefield(player1, new StrataScythe());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipCannotTargetOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new PalladiumMyr());
        harness.addToBattlefield(player1, new StrataScythe());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equip cannot be activated outside a main phase")
    void equipCannotBeActivatedDuringCombat() {
        Permanent creature = addCreatureReady(player1, new PalladiumMyr());
        harness.addToBattlefield(player1, new StrataScythe());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("ETB presents only land cards from library for imprint choice")
    void etbPresentsLandCardsForImprintChoice() {
        Plains plains = new Plains();
        PalladiumMyr myr = new PalladiumMyr();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(plains, myr, forest));
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new StrataScythe(), "{3}");
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1.getId());
        // Only land cards should be presented (Plains and Forest, not Palladium Myr)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND));
    }

    @Test
    @DisplayName("Choosing a land exiles it and imprints on Strata Scythe")
    void choosingLandExilesAndImprints() {
        Plains plains = new Plains();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(plains, forest));
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new StrataScythe(), "{3}");
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Choose the first card (Plains)
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        // Plains should be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));

        // Strata Scythe should have Plains imprinted
        Permanent scythe = findPermanent(player1, "Strata Scythe");
        assertThat(gd.getImprintedCard(scythe.getCard())).isNotNull();
        assertThat(gd.getImprintedCard(scythe.getCard()).getName()).isEqualTo("Plains");

        // Library should be shuffled (only Forest remains)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Player may fail to find a land card")
    void playerMayFailToFind() {
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new StrataScythe(), "{3}");
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Decline to find (-1 means fail to find)
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        // No card exiled
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        // No imprint
        Permanent scythe = findPermanent(player1, "Strata Scythe");
        assertThat(gd.getImprintedCard(scythe.getCard())).isNull();
    }

    @Test
    @DisplayName("ETB does nothing with empty library")
    void etbDoesNothingWithEmptyLibrary() {
        gd.playerDecks.get(player1.getId()).clear();
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new StrataScythe(), "{3}");
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("library") && log.contains("empty"));
    }

    @Test
    @DisplayName("ETB shuffles and logs when no land cards in library")
    void etbNoLandCardsInLibrary() {
        harness.setLibrary(player1, List.of(new PalladiumMyr(), new PalladiumMyr()));
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new StrataScythe(), "{3}");
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no land cards"));
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each matching land on the battlefield")
    void equippedCreatureGetsBoostPerMatchingLand() {
        Permanent creature = addCreatureReady(player1, new PalladiumMyr()); // 2/2
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new StrataScythe());

        // Imprint Plains
        Plains imprintedPlains = new Plains();
        gd.addToExile(player1.getId(), imprintedPlains, scythe.getId());
        gd.setImprintedCard(scythe.getCard(), imprintedPlains);

        // Attach to creature
        scythe.setAttachedTo(creature.getId());

        // Add 2 Plains to the battlefield
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());

        // 2/2 base + 2 Plains = 4/4
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost counts lands on all battlefields, not just controller's")
    void boostCountsLandsOnAllBattlefields() {
        Permanent creature = addCreatureReady(player1, new PalladiumMyr()); // 2/2
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new StrataScythe());

        // Imprint Plains
        Plains imprintedPlains = new Plains();
        gd.addToExile(player1.getId(), imprintedPlains, scythe.getId());
        gd.setImprintedCard(scythe.getCard(), imprintedPlains);
        scythe.setAttachedTo(creature.getId());

        // 1 Plains on player1's battlefield
        harness.addToBattlefield(player1, new Plains());
        // 2 Plains on player2's battlefield
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Plains());

        // 2/2 base + 3 Plains total = 5/5
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("No boost when no matching lands are on the battlefield")
    void noBoostWhenNoMatchingLands() {
        Permanent creature = addCreatureReady(player1, new PalladiumMyr()); // 2/2
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new StrataScythe());

        // Imprint Plains
        Plains imprintedPlains = new Plains();
        gd.addToExile(player1.getId(), imprintedPlains, scythe.getId());
        gd.setImprintedCard(scythe.getCard(), imprintedPlains);
        scythe.setAttachedTo(creature.getId());

        // Only Mountains on battlefield, no Plains
        harness.addToBattlefield(player1, new Mountain());

        // 2/2 base + 0 matching = 2/2
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("No boost when no card is imprinted")
    void noBoostWhenNoImprint() {
        Permanent creature = addCreatureReady(player1, new PalladiumMyr()); // 2/2
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new StrataScythe());
        scythe.setAttachedTo(creature.getId());

        // Plains on battlefield but no imprint
        harness.addToBattlefield(player1, new Plains());

        // 2/2 base, no boost
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost does not affect unequipped creatures")
    void boostDoesNotAffectUnequippedCreatures() {
        Permanent creature1 = addCreatureReady(player1, new PalladiumMyr()); // 2/2
        Permanent creature2 = addCreatureReady(player1, new PalladiumMyr()); // 2/2
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new StrataScythe());

        Plains imprintedPlains = new Plains();
        gd.addToExile(player1.getId(), imprintedPlains, scythe.getId());
        gd.setImprintedCard(scythe.getCard(), imprintedPlains);
        scythe.setAttachedTo(creature1.getId());
        harness.addToBattlefield(player1, new Plains());

        // creature2 should not get any boost
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature2)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost moves when equipment is re-equipped to another creature")
    void boostMovesOnReEquip() {
        Permanent creature1 = addCreatureReady(player1, new PalladiumMyr()); // 2/2
        Permanent creature2 = addCreatureReady(player1, new PalladiumMyr()); // 2/2
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new StrataScythe());

        Plains imprintedPlains = new Plains();
        gd.addToExile(player1.getId(), imprintedPlains, scythe.getId());
        gd.setImprintedCard(scythe.getCard(), imprintedPlains);
        scythe.setAttachedTo(creature1.getId());
        harness.addToBattlefield(player1, new Plains());

        // creature1 gets boost
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(3);

        // Move equipment to creature2
        scythe.setAttachedTo(creature2.getId());

        // creature1 loses boost, creature2 gains it
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(3);
    }

    @Test
    @DisplayName("The searched land is exiled face up")
    void searchedLandIsExiledFaceUp() {
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new StrataScythe(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.findExiledCard(plains.getId())).isNotNull();
        assertThat(gd.findExiledCard(plains.getId()).faceDown()).isFalse();
    }

    @Test
    @DisplayName("Boost stops when the imprinted land leaves exile")
    void boostStopsWhenImprintedLandLeavesExile() {
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));
        Permanent creature = addCreatureReady(player1, new PalladiumMyr());
        harness.addToBattlefield(player1, new Plains());
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new StrataScythe(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 2, null, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        assertThat(gd.removeFromExile(plains.getId())).isTrue();
        gd.playerHands.get(player1.getId()).add(plains);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
