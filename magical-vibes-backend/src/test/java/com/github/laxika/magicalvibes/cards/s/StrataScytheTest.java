package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturePerMatchingLandNameEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypeToExileAndImprintEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StrataScytheTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has imprint ETB search effect for land cards")
    void hasImprintEtbEffect() {
        StrataScythe card = new StrataScythe();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(SearchLibraryForCardTypeToExileAndImprintEffect.class);
        var etb = (SearchLibraryForCardTypeToExileAndImprintEffect)
                card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(etb.cardTypes()).containsExactly(CardType.LAND);
    }

    @Test
    @DisplayName("Has static boost effect per matching land name")
    void hasStaticBoostEffect() {
        StrataScythe card = new StrataScythe();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(BoostCreaturePerMatchingLandNameEffect.class);
        var boost = (BoostCreaturePerMatchingLandNameEffect)
                card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(boost.powerPerMatch()).isEqualTo(1);
        assertThat(boost.toughnessPerMatch()).isEqualTo(1);
    }

    @Test
    @DisplayName("Has equip {3} ability with sorcery-speed restriction")
    void hasEquipAbility() {
        StrataScythe card = new StrataScythe();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.getManaCost()).isEqualTo("{3}");
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getTargetFilter()).isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(ability.getTimingRestriction()).isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(ability.getEffects()).singleElement().isInstanceOf(EquipEffect.class);
    }

    // ===== ETB imprint search =====

    @Test
    @DisplayName("ETB presents only land cards from library for imprint choice")
    void etbPresentsLandCardsForImprintChoice() {
        Plains plains = new Plains();
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        setupDeck(player1, List.of(plains, bears, forest));
        harness.setHand(player1, List.of(new StrataScythe()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1.getId());
        // Only land cards should be presented (Plains and Forest, not Grizzly Bears)
        assertThat(gd.interaction.librarySearch().cards()).hasSize(2);
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.hasType(CardType.LAND));
    }

    @Test
    @DisplayName("Choosing a land exiles it and imprints on Strata Scythe")
    void choosingLandExilesAndImprints() {
        Plains plains = new Plains();
        Forest forest = new Forest();
        setupDeck(player1, List.of(plains, forest));
        harness.setHand(player1, List.of(new StrataScythe()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Choose the first card (Plains)
        gs.handleLibraryCardChosen(gd, player1, 0);

        // Plains should be in exile
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));

        // Strata Scythe should have Plains imprinted
        Permanent scythe = findPermanent(player1, "Strata Scythe");
        assertThat(scythe.getCard().getImprintedCard()).isNotNull();
        assertThat(scythe.getCard().getImprintedCard().getName()).isEqualTo("Plains");

        // Library should be shuffled (only Forest remains)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Player may fail to find a land card")
    void playerMayFailToFind() {
        Plains plains = new Plains();
        setupDeck(player1, List.of(plains));
        harness.setHand(player1, List.of(new StrataScythe()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Decline to find (-1 means fail to find)
        gs.handleLibraryCardChosen(gd, player1, -1);

        // No card exiled
        assertThat(gd.playerExiledCards.get(player1.getId())).isEmpty();

        // No imprint
        Permanent scythe = findPermanent(player1, "Strata Scythe");
        assertThat(scythe.getCard().getImprintedCard()).isNull();
    }

    @Test
    @DisplayName("ETB does nothing with empty library")
    void etbDoesNothingWithEmptyLibrary() {
        gd.playerDecks.get(player1.getId()).clear();
        harness.setHand(player1, List.of(new StrataScythe()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("library") && log.contains("empty"));
    }

    @Test
    @DisplayName("ETB shuffles and logs when no land cards in library")
    void etbNoLandCardsInLibrary() {
        setupDeck(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.setHand(player1, List.of(new StrataScythe()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no land cards"));
    }

    // ===== Static boost =====

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each matching land on the battlefield")
    void equippedCreatureGetsBoostPerMatchingLand() {
        Permanent creature = addReadyCreature(player1); // 2/2
        Permanent scythe = addReadyScythe(player1);

        // Imprint Plains
        Plains imprintedPlains = new Plains();
        scythe.getCard().setImprintedCard(imprintedPlains);

        // Attach to creature
        scythe.setAttachedTo(creature.getId());

        // Add 2 Plains to the battlefield
        addReadyLand(player1, new Plains());
        addReadyLand(player1, new Plains());

        // 2/2 base + 2 Plains = 4/4
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost counts lands on all battlefields, not just controller's")
    void boostCountsLandsOnAllBattlefields() {
        Permanent creature = addReadyCreature(player1); // 2/2
        Permanent scythe = addReadyScythe(player1);

        // Imprint Plains
        scythe.getCard().setImprintedCard(new Plains());
        scythe.setAttachedTo(creature.getId());

        // 1 Plains on player1's battlefield
        addReadyLand(player1, new Plains());
        // 2 Plains on player2's battlefield
        addReadyLand(player2, new Plains());
        addReadyLand(player2, new Plains());

        // 2/2 base + 3 Plains total = 5/5
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("No boost when no matching lands are on the battlefield")
    void noBoostWhenNoMatchingLands() {
        Permanent creature = addReadyCreature(player1); // 2/2
        Permanent scythe = addReadyScythe(player1);

        // Imprint Plains
        scythe.getCard().setImprintedCard(new Plains());
        scythe.setAttachedTo(creature.getId());

        // Only Mountains on battlefield, no Plains
        addReadyLand(player1, new Mountain());

        // 2/2 base + 0 matching = 2/2
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("No boost when no card is imprinted")
    void noBoostWhenNoImprint() {
        Permanent creature = addReadyCreature(player1); // 2/2
        Permanent scythe = addReadyScythe(player1);
        scythe.setAttachedTo(creature.getId());

        // Plains on battlefield but no imprint
        addReadyLand(player1, new Plains());

        // 2/2 base, no boost
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost does not affect unequipped creatures")
    void boostDoesNotAffectUnequippedCreatures() {
        Permanent creature1 = addReadyCreature(player1); // 2/2
        Permanent creature2 = addReadyCreature(player1); // 2/2
        Permanent scythe = addReadyScythe(player1);

        scythe.getCard().setImprintedCard(new Plains());
        scythe.setAttachedTo(creature1.getId());
        addReadyLand(player1, new Plains());

        // creature2 should not get any boost
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature2)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost moves when equipment is re-equipped to another creature")
    void boostMovesOnReEquip() {
        Permanent creature1 = addReadyCreature(player1); // 2/2
        Permanent creature2 = addReadyCreature(player1); // 2/2
        Permanent scythe = addReadyScythe(player1);

        scythe.getCard().setImprintedCard(new Plains());
        scythe.setAttachedTo(creature1.getId());
        addReadyLand(player1, new Plains());

        // creature1 gets boost
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(3);

        // Move equipment to creature2
        scythe.setAttachedTo(creature2.getId());

        // creature1 loses boost, creature2 gains it
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(3);
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyScythe(Player player) {
        Permanent perm = new Permanent(new StrataScythe());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyLand(Player player, Card landCard) {
        Permanent perm = new Permanent(landCard);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupDeck(Player player, List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
