package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwordOfTheAnimist.class, TimberpackWolf.class, Plains.class, Forest.class, Island.class, EvolvingWilds.class})
class SwordOfTheAnimistTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new TimberpackWolf());
        Permanent sword = addCreatureReady(player1, new SwordOfTheAnimist());
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking with the equipped creature offers a basic land search")
    void attackingOffersLandSearch() {
        equipAndAttack();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(3);
    }

    @Test
    @DisplayName("The chosen basic land enters the battlefield tapped")
    void chosenLandEntersTapped() {
        equipAndAttack();
        harness.handleMayAbilityChosen(player1, true);

        String chosenName = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst().getName();
        harness.handleCardChosen(player1, 0);

        Permanent land = findPermanent(player1, chosenName);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the trigger leaves the library untouched")
    void decliningSkipsSearch() {
        equipAndAttack();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("An unequipped Sword of the Animist doesn't trigger on attack")
    void unequippedSwordDoesNotTrigger() {
        addCreatureReady(player1, new TimberpackWolf());
        addCreatureReady(player1, new SwordOfTheAnimist());
        setupLibrary();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    void equipPaysTwoManaAndAttachesOnResolution() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTheAnimist());
        Permanent creature = addCreatureReady(player1, new TimberpackWolf());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(sword.getAttachedTo()).isNull();
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void cannotEquipOpponentsCreature() {
        harness.addToBattlefield(player1, new SwordOfTheAnimist());
        Permanent creature = addCreatureReady(player2, new TimberpackWolf());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipDuringCombat() {
        harness.addToBattlefield(player1, new SwordOfTheAnimist());
        Permanent creature = addCreatureReady(player1, new TimberpackWolf());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayFailToFindEvenWithBasicLandsAvailable() {
        equipAndAttack();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void nonbasicLandsAreExcludedFromSearch() {
        equipAndAttack();
        Plains plains = new Plains();
        EvolvingWilds wilds = new EvolvingWilds();
        harness.setLibrary(player1, List.of(wilds, plains));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(plains);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(wilds);
        assertThat(findPermanent(player1, "Plains").isTapped()).isTrue();
    }

    @Test
    void searchWithNoBasicLandsCompletesWithoutMovingCards() {
        equipAndAttack();
        EvolvingWilds wilds = new EvolvingWilds();
        harness.setLibrary(player1, List.of(wilds));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(wilds);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void searchWithEmptyLibraryCompletes() {
        equipAndAttack();
        harness.setLibrary(player1, List.of());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void triggerResolvesAfterSwordLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new TimberpackWolf());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTheAnimist());
        sword.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Forest()));
        declareAttackers(player1, List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(sword);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void swordControllerSearchesWhenOpponentsEquippedCreatureAttacks() {
        Permanent creature = addCreatureReady(player2, new TimberpackWolf());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTheAnimist());
        sword.setAttachedTo(creature.getId());
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest));
        harness.setLibrary(player2, List.of(island));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(island);
        assertThat(countPermanents(player2, "Forest")).isZero();
    }

    private void equipAndAttack() {
        Permanent creature = addCreatureReady(player1, new TimberpackWolf());
        Permanent sword = addCreatureReady(player1, new SwordOfTheAnimist());
        sword.setAttachedTo(creature.getId());
        setupLibrary();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new TimberpackWolf()));
    }
}
