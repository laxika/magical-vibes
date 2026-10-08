package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FlametongueYearling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordOfHearthAndHome.class, GrizzlyBears.class, Forest.class, FlametongueYearling.class})
class SwordOfHearthAndHomeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2 and protection from green and white")
    void equippedCreatureGetsBoostAndProtection() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady();
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Combat damage flickers an owned creature and puts a basic land onto the battlefield")
    void combatDamageFlickersCreatureAndSearchesBasicLand() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady();
        sword.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveCombat();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(creature.getId(), player1.getId())
                .doesNotContain(sword.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Forest");
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1)
                .doesNotContain(creature);
        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The trigger can resolve without choosing a creature")
    void combatDamageCanSkipCreatureFlicker() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady();
        sword.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
    }

    @Test
    void equipAttachesForTwoMana() {
        Permanent sword = addSwordReady();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void combatDamageToCreatureDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady();
        sword.setAttachedTo(attacker.getId());
        addCreatureReady(player2, new FlametongueYearling());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureStaysExiledUntilLandIsSelected() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady();
        sword.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.findExiledCard(creature.getCard().getId())).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void canRecoverOwnedCreatureControlledByOpponent() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady();
        sword.setAttachedTo(attacker.getId());
        Permanent stolen = addCreatureReady(player2, new GrizzlyBears());
        gd.stolenCreatures.put(stolen.getId(), player1.getId());
        stolen.tap();
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        resolveCombat();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(stolen.getId());
        harness.handlePermanentChosen(player1, stolen.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
    }

    @Test
    void illegalSoleTargetPreventsLandSearch() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady();
        sword.setAttachedTo(attacker.getId());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    void creatureReturnsEvenWhenNoBasicLandCanBeFound() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady();
        sword.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1).doesNotContain(creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(sword.getAttachedTo()).isNull();
    }

    private Permanent addSwordReady() {
        return addCreatureReady(player1, new SwordOfHearthAndHome());
    }
}
