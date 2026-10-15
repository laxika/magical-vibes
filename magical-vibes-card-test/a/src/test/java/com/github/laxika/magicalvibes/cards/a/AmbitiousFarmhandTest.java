package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CatharCommando;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HinterlandLogger;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({AmbitiousFarmhand.class, CatharCommando.class, HinterlandLogger.class, Plains.class, Forest.class})
class AmbitiousFarmhandTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may search puts a basic Plains into hand")
    void etbSearchPutsBasicPlainsInHand() {
        castFarmhand();
        setupLibrary();

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(c -> c.getName()).containsExactly("Plains");

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("Declining the ETB search puts nothing in hand")
    void decliningEtbSearchDoesNothing() {
        castFarmhand();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotInHand(player1, "Plains");
    }

    @Test
    @DisplayName("Cannot activate coven transform without three different powers")
    void cannotTransformWithoutCoven() {
        Permanent farmhand = addCreatureReady(player1, new AmbitiousFarmhand());
        harness.addToBattlefield(player1, new AmbitiousFarmhand());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(farmhand), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different powers");
    }

    @Test
    @DisplayName("Coven transform turns Ambitious Farmhand into Seasoned Cathar")
    void covenTransformsIntoSeasonedCathar() {
        Permanent farmhand = addCreatureReady(player1, new AmbitiousFarmhand());
        harness.addToBattlefield(player1, new HinterlandLogger());
        harness.addToBattlefield(player1, new CatharCommando());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, indexOf(farmhand), null, null);
        harness.passBothPriorities();

        assertThat(farmhand.isTransformed()).isTrue();
        assertThat(farmhand.getCard().getName()).isEqualTo("Seasoned Cathar");
    }

    @Test
    @DisplayName("An accepted search may fail to find even when a Plains is available")
    void mayFailToFindAvailablePlains() {
        castFarmhand();
        setupLibrary();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An accepted search with no Plains completes without adding a card")
    void searchWithNoMatchingCardCompletes() {
        castFarmhand();
        harness.setLibrary(player1, List.of(new Forest(), new HinterlandLogger()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Three creatures with only two different powers do not satisfy coven")
    void threeCreaturesWithDuplicatePowersCannotTransform() {
        Permanent farmhand = addCreatureReady(player1, new AmbitiousFarmhand());
        harness.addToBattlefield(player1, new AmbitiousFarmhand());
        harness.addToBattlefield(player1, new HinterlandLogger());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(farmhand), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different powers");
    }

    @Test
    @DisplayName("Opposing creatures do not count toward coven")
    void opponentCreaturesDoNotEnableCoven() {
        Permanent farmhand = addCreatureReady(player1, new AmbitiousFarmhand());
        harness.addToBattlefield(player2, new HinterlandLogger());
        harness.addToBattlefield(player2, new CatharCommando());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(farmhand), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different powers");
    }

    @Test
    @DisplayName("Losing coven after activation does not stop transformation")
    void covenIsNotRecheckedOnResolution() {
        Permanent farmhand = addCreatureReady(player1, new AmbitiousFarmhand());
        harness.addToBattlefield(player1, new HinterlandLogger());
        harness.addToBattlefield(player1, new CatharCommando());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, indexOf(farmhand), null, null);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p != farmhand);
        harness.passBothPriorities();

        assertThat(farmhand.isTransformed()).isTrue();
        harness.assertOnBattlefield(player1, "Seasoned Cathar");
    }

    @Test
    @DisplayName("Coven can transform a tapped, summoning-sick Farmhand")
    void tappedSummoningSickFarmhandCanTransform() {
        Permanent farmhand = harness.addToBattlefieldAndReturn(player1, new AmbitiousFarmhand());
        farmhand.setSummoningSick(true);
        farmhand.tap();
        harness.addToBattlefield(player1, new HinterlandLogger());
        harness.addToBattlefield(player1, new CatharCommando());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, indexOf(farmhand), null, null);
        harness.passBothPriorities();

        assertThat(farmhand.isTransformed()).isTrue();
        assertThat(farmhand.isTapped()).isTrue();
        assertThat(farmhand.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Two pending transform activations transform the permanent only once")
    void multipleActivationsDoNotTransformBack() {
        Permanent farmhand = addCreatureReady(player1, new AmbitiousFarmhand());
        harness.addToBattlefield(player1, new HinterlandLogger());
        harness.addToBattlefield(player1, new CatharCommando());
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.activateAbility(player1, indexOf(farmhand), null, null);
        harness.activateAbility(player1, indexOf(farmhand), null, null);
        resolveAllTriggers();

        assertThat(farmhand.isTransformed()).isTrue();
        harness.assertOnBattlefield(player1, "Seasoned Cathar");
    }

    @Test
    @DisplayName("Seasoned Cathar gains life when it deals combat damage")
    void transformedCatharHasLifelinkInCombat() {
        Permanent farmhand = addCreatureReady(player1, new AmbitiousFarmhand());
        harness.addToBattlefield(player1, new HinterlandLogger());
        harness.addToBattlefield(player1, new CatharCommando());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, indexOf(farmhand), null, null);
        harness.passBothPriorities();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(indexOf(farmhand)));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Ambitious Farmhand does not have the back face's lifelink")
    void frontFaceDoesNotGainLifeInCombat() {
        Permanent farmhand = addCreatureReady(player1, new AmbitiousFarmhand());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(indexOf(farmhand)));
        resolveCombat();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    private void castFarmhand() {
        harness.setHand(player1, List.of(new AmbitiousFarmhand()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new HinterlandLogger()));
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
