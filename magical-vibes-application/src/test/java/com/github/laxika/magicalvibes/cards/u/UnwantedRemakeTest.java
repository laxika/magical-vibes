package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TyvarThePummeler;
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

@CardUsed({UnwantedRemake.class, GrizzlyBears.class, Forest.class, Mountain.class, TyvarThePummeler.class})
class UnwantedRemakeTest extends BaseCardTest {

    @Test
    void destroysCreatureAndItsControllerManifestsDread() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new UnwantedRemake()));
        harness.setLibrary(player2, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);

        harness.handleMultipleCardsChosen(player2, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(graveyardCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new UnwantedRemake()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void canDestroyOwnCreatureAndTurnManifestedCreatureFaceUpForItsManaCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new UnwantedRemake()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.getCard()).isEqualTo(manifestedCard);
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard(), graveyardCard);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, 0);

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canManifestNoncreatureFromOneCardLibraryButCannotTurnItFaceUp() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card manifestedCard = new Forest();
        harness.setHand(player1, List.of(new UnwantedRemake()));
        harness.setLibrary(player2, List.of(manifestedCard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMultipleCardsChosen(player2, List.of(manifestedCard.getId()));

        Permanent manifested = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(manifested.getCard()).isEqualTo(manifestedCard);
        assertThat(manifested.isManifested()).isTrue();
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target.getCard());
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThatThrownBy(() -> harness.turnFaceUp(player2, 0)).isInstanceOf(IllegalStateException.class);
        assertThat(manifested.isFaceDown()).isTrue();
    }

    @Test
    void stillDestroysCreatureWhenItsControllersLibraryIsEmpty() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnwantedRemake()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target.getCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotManifestDreadWhenTargetHasLeftBattlefieldBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card manifestedCard = new Forest();
        Card graveyardCard = new Mountain();
        Card untouchedCard = new Forest();
        harness.setHand(player1, List.of(new UnwantedRemake(), new UnwantedRemake()));
        harness.setLibrary(player2, List.of(manifestedCard, graveyardCard, untouchedCard));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMultipleCardsChosen(player2, List.of(manifestedCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(untouchedCard);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target.getCard(), graveyardCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controllerStillManifestsDreadWhenCreatureIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TyvarThePummeler());
        Permanent helper = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card manifestedCard = new Forest();
        Card graveyardCard = new Mountain();
        harness.setHand(player1, List.of(new UnwantedRemake()));
        harness.setLibrary(player2, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMultipleCardsChosen(player2, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target, helper).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
