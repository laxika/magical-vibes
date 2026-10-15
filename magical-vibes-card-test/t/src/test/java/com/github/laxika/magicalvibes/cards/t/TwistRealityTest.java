package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BashfulBeastie;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwistReality.class, GrizzlyBears.class, Forest.class, BashfulBeastie.class})
class TwistRealityTest extends BaseCardTest {

    @Test
    void countersTargetSpellWithFirstMode() {
        GrizzlyBears spell = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new TwistReality()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, spell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Twist Reality");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rejectsNonSpellTargetForCounterMode() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new TwistReality()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 0,
                harness.getPermanentId(player1, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countersAnInstantWithoutPerformingEitherManifestMode() {
        TwistReality spell = new TwistReality();
        Card firstLibraryCard = new Forest();
        Card secondLibraryCard = new Forest();
        harness.setLibrary(player1, List.of(firstLibraryCard));
        harness.setLibrary(player2, List.of(secondLibraryCard));
        harness.setHand(player1, List.of(spell));
        harness.setHand(player2, List.of(new TwistReality()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, 1, null);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, spell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Twist Reality");
        harness.assertInGraveyard(player2, "Twist Reality");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstLibraryCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondLibraryCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void counterModeRequiresATargetEvenThoughTheOtherModeDoesNot() {
        harness.setHand(player1, List.of(new TwistReality()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Twist Reality");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manifestsOneOfTopTwoAndPutsTheOtherInGraveyardWithSecondMode() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.setHand(player1, List.of(new TwistReality()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        harness.assertInGraveyard(player1, "Twist Reality");
    }

    @Test
    void canManifestTheSecondCardEvenWhenItIsALand() {
        Card firstCard = new BashfulBeastie();
        Card land = new Forest();
        Card untouchedCard = new TwistReality();
        harness.setLibrary(player1, List.of(firstCard, land, untouchedCard));
        harness.setHand(player1, List.of(new TwistReality()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.getCard()).isSameAs(land);
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.isManifested()).isTrue();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstCard).doesNotContain(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouchedCard);
        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(manifested.isFaceDown()).isTrue();
    }

    @Test
    void manifestsTheOnlyCardWithoutPuttingItIntoTheGraveyard() {
        Card onlyCard = new BashfulBeastie();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new TwistReality()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(onlyCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard()).isSameAs(onlyCard);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isManifested()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryDoesNothingAndDoesNotRequireAChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new TwistReality()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Twist Reality");
    }

    @Test
    void manifestedCreatureTurnsFaceUpForItsManaCostWithoutUsingTheStack() {
        Card creature = new BashfulBeastie();
        harness.setLibrary(player1, List.of(creature, new Forest()));
        harness.setHand(player1, List.of(new TwistReality()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(manifested.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, 0);

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.isManifested()).isFalse();
        assertThat(manifested.getCard()).isSameAs(creature);
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }
}
