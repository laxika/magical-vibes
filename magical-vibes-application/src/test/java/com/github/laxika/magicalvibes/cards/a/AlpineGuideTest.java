package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlpineGuide.class, Mountain.class, Forest.class, LightningBolt.class})
class AlpineGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Alpine Guide may put a Mountain from the library onto the battlefield tapped")
    void maySearchForMountain() {
        harness.setHand(player1, List.of(new AlpineGuide()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLibrary(player1, List.of(new Mountain(), new Forest()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).singleElement()
                .satisfies(card -> assertThat(card.getSubtypes()).contains(CardSubtype.MOUNTAIN));

        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Mountain").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Declining Alpine Guide's search leaves the library unchanged")
    void maySearchCanBeDeclined() {
        harness.setHand(player1, List.of(new AlpineGuide()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLibrary(player1, List.of(new Mountain()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Mountain"));
    }

    @Test
    @DisplayName("When Alpine Guide leaves, its controller sacrifices a Mountain")
    void sacrificesMountainWhenLeavingBattlefield() {
        Permanent guide = addCreatureReady(player1, new AlpineGuide());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, guide.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Alpine Guide");
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Forest"));
    }

    @Test
    @DisplayName("Alpine Guide must attack when able")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new AlpineGuide());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Alpine Guide can be declared as an attacker")
    void canAttackWhenAble() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new AlpineGuide());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Alpine Guide's controller may fail to find even with a Mountain in the library")
    void mayFailToFindMountain() {
        harness.setHand(player1, List.of(new AlpineGuide()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLibrary(player1, List.of(new Mountain(), new Forest()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("Accepting Alpine Guide's search with no Mountains completes without finding a card")
    void searchWithNoMountain() {
        harness.setHand(player1, List.of(new AlpineGuide()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Alpine Guide's controller chooses exactly one of their Mountains to sacrifice")
    void choosesOneMountainToSacrifice() {
        Permanent guide = addCreatureReady(player1, new AlpineGuide());
        harness.addToBattlefield(player1, new Mountain());
        Permanent chosenMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, guide.getId());
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosenMountain.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(chosenMountain.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Mountain"))
                .hasSize(1);
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("Leaving with no Mountain does not sacrifice another land or an opponent's Mountain")
    void leavingWithoutMountainDoesNothing() {
        Permanent guide = addCreatureReady(player1, new AlpineGuide());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, guide.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Alpine Guide");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("A tapped Alpine Guide is not required to attack")
    void tappedGuideNeedNotAttack() {
        Permanent guide = addCreatureReady(player1, new AlpineGuide());
        guide.setTapped(true);

        declareAttackers(List.of());

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Alpine Guide");
    }

    @Test
    @DisplayName("A summoning-sick Alpine Guide is not required to attack")
    void summoningSickGuideNeedNotAttack() {
        harness.addToBattlefield(player1, new AlpineGuide());

        declareAttackers(List.of());

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Alpine Guide");
    }
}
