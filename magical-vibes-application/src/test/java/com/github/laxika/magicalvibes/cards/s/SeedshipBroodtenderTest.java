package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LumenClassFrigate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeedshipBroodtender.class, Forest.class, GrizzlyBears.class, LumenClassFrigate.class})
class SeedshipBroodtenderTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, it mills three cards")
    void entersMillsThreeCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castSeedshipBroodtender();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Its activated ability sacrifices it and returns a creature")
    void returnsCreatureFromGraveyard() {
        Permanent broodtender = harness.addToBattlefieldAndReturn(player1, new SeedshipBroodtender());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(broodtender), 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Seedship Broodtender");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Its activated ability also returns a Spacecraft")
    void returnsSpacecraftFromGraveyard() {
        Permanent broodtender = harness.addToBattlefieldAndReturn(player1, new SeedshipBroodtender());
        Card spacecraft = new LumenClassFrigate();
        harness.setGraveyard(player1, List.of(spacecraft));
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(broodtender), 0, null, spacecraft.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Seedship Broodtender");
        harness.assertOnBattlefield(player1, "Lumen-Class Frigate");
    }

    @Test
    @DisplayName("Its activated ability rejects a noncreature, non-Spacecraft card")
    void rejectsInvalidGraveyardTarget() {
        Permanent broodtender = harness.addToBattlefieldAndReturn(player1, new SeedshipBroodtender());
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(broodtender), 0, null, land.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Seedship Broodtender");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void millsOnlyThreeCardsFromItsControllersLibrary() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        Card opponentsCard = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setLibrary(player2, List.of(opponentsCard));

        castSeedshipBroodtender();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void millsAllRemainingCardsWhenLibraryHasFewerThanThree() {
        Card card = new Forest();
        harness.setLibrary(player1, List.of(card));

        castSeedshipBroodtender();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        harness.assertOnBattlefield(player1, "Seedship Broodtender");
    }

    @Test
    void sacrificesAsACostBeforeReturningTarget() {
        Permanent broodtender = harness.addToBattlefieldAndReturn(player1, new SeedshipBroodtender());
        Card spacecraft = new LumenClassFrigate();
        harness.setGraveyard(player1, List.of(spacecraft));
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(broodtender), 0, null, spacecraft.getId(), Zone.GRAVEYARD);

        harness.assertNotOnBattlefield(player1, "Seedship Broodtender");
        harness.assertInGraveyard(player1, "Seedship Broodtender");
        harness.assertNotOnBattlefield(player1, "Lumen-Class Frigate");
        harness.assertInGraveyard(player1, "Lumen-Class Frigate");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lumen-Class Frigate");
        harness.assertInGraveyard(player1, "Seedship Broodtender");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spacecraft);
    }

    @Test
    void rejectsOpponentsGraveyardTarget() {
        Permanent broodtender = harness.addToBattlefieldAndReturn(player1, new SeedshipBroodtender());
        Card spacecraft = new LumenClassFrigate();
        harness.setGraveyard(player2, List.of(spacecraft));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(broodtender), 0, null, spacecraft.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Seedship Broodtender");
        harness.assertInGraveyard(player2, "Lumen-Class Frigate");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetItselfBeforePayingSacrificeCost() {
        SeedshipBroodtender card = new SeedshipBroodtender();
        Permanent broodtender = harness.addToBattlefieldAndReturn(player1, card);
        harness.setGraveyard(player1, List.of());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(broodtender), 0, null, card.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Seedship Broodtender");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent broodtender = harness.addToBattlefieldAndReturn(player1, new SeedshipBroodtender());
        Card spacecraft = new LumenClassFrigate();
        harness.setGraveyard(player1, List.of(spacecraft));
        addActivationMana();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(broodtender), 0, null, spacecraft.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Seedship Broodtender");
        harness.assertInGraveyard(player1, "Lumen-Class Frigate");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileAnotherSpellIsOnStack() {
        Permanent broodtender = harness.addToBattlefieldAndReturn(player1, new SeedshipBroodtender());
        Card spacecraft = new LumenClassFrigate();
        harness.setGraveyard(player1, List.of(spacecraft));
        harness.setHand(player1, List.of(new SeedshipBroodtender()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(broodtender), 0, null, spacecraft.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Seedship Broodtender");
        harness.assertInGraveyard(player1, "Lumen-Class Frigate");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReturnTargetThatLeavesGraveyardBeforeResolution() {
        Permanent broodtender = harness.addToBattlefieldAndReturn(player1, new SeedshipBroodtender());
        Card spacecraft = new LumenClassFrigate();
        harness.setGraveyard(player1, List.of(spacecraft));
        addActivationMana();
        harness.activateAbility(player1, battlefieldIndex(broodtender), 0, null, spacecraft.getId(), Zone.GRAVEYARD);

        harness.setGraveyard(player1, List.of(broodtender.getCard()));
        harness.setExile(player1, List.of(spacecraft));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lumen-Class Frigate");
        harness.assertInGraveyard(player1, "Seedship Broodtender");
        assertThat(gd.findExiledCard(spacecraft.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        Permanent broodtender = harness.addToBattlefieldAndReturn(player1, new SeedshipBroodtender());
        Card spacecraft = new LumenClassFrigate();
        harness.setGraveyard(player1, List.of(spacecraft));
        addActivationMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(broodtender), 0, null, spacecraft.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Seedship Broodtender");
        harness.assertInGraveyard(player1, "Lumen-Class Frigate");
        assertThat(gd.stack).isEmpty();
    }

    private void castSeedshipBroodtender() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SeedshipBroodtender()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
