package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheBiblioplex.class, Divination.class, Forest.class, GrizzlyBears.class, Shock.class})
class TheBiblioplexTest extends BaseCardTest {

    @Test
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new TheBiblioplex());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void acceptsMatchingInstantIntoHandWithEmptyHand() {
        Shock shock = new Shock();
        harness.addToBattlefield(player1, new TheBiblioplex());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(shock, new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateLookAbility();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(shock);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
    }

    @Test
    void acceptsMatchingSorceryWithSevenCardsInHand() {
        Divination divination = new Divination();
        harness.addToBattlefield(player1, new TheBiblioplex());
        harness.setHand(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()
        ));
        harness.setLibrary(player1, List.of(divination));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateLookAbility();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(divination).hasSize(8);
    }

    @Test
    void mayPutUnacceptedTopCardIntoGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, new TheBiblioplex());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateLookAbility();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    void cannotActivateWithAnyOtherHandSize() {
        harness.addToBattlefield(player1, new TheBiblioplex());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.setHand(player1, List.of(new Forest()));
        assertThatThrownBy(this::activateLookAbility).isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()
        ));
        assertThatThrownBy(this::activateLookAbility).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void declinedMatchingCardMayGoToGraveyard() {
        Shock shock = new Shock();
        harness.addToBattlefield(player1, new TheBiblioplex());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(shock, new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateLookAbility();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
    }

    @Test
    void decliningBothChoicesLeavesMatchingCardOnTop() {
        Shock shock = new Shock();
        harness.addToBattlefield(player1, new TheBiblioplex());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(shock, new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateLookAbility();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(shock);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock);
    }

    @Test
    void decliningGraveyardChoiceLeavesNonmatchingCardOnTop() {
        Forest forest = new Forest();
        harness.addToBattlefield(player1, new TheBiblioplex());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateLookAbility();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    void emptyLibraryResolvesWithoutAChoice() {
        harness.addToBattlefield(player1, new TheBiblioplex());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateLookAbility();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void handSizeRestrictionIsNotRecheckedAtResolution() {
        Shock shock = new Shock();
        Forest forest = new Forest();
        harness.addToBattlefield(player1, new TheBiblioplex());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(shock));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.setHand(player1, List.of(forest));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest, shock);
    }

    private void activateLookAbility() {
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
    }
}
