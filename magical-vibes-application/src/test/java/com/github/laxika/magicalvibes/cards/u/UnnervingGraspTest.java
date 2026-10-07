package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.ParanormalAnalyst;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnnervingGrasp.class, GrizzlyBears.class, Forest.class, Mountain.class, ParanormalAnalyst.class})
class UnnervingGraspTest extends BaseCardTest {

    @Test
    void returnsTargetNonlandPermanentAndManifestsDread() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        prepareSpell(List.of(manifestedCard, graveyardCard));

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
    }

    @Test
    void canResolveWithoutChoosingTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Mountain();
        prepareSpell(List.of(manifestedCard, graveyardCard));

        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
    }

    @Test
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new UnnervingGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    void manifestsTheOnlyLibraryCardEvenWhenItIsALand() {
        Card onlyCard = new Forest();
        prepareSpell(List.of(onlyCard));

        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        harness.handleMultipleCardsChosen(player1, List.of(onlyCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.isManifested()).isTrue();
                    assertThat(permanent.isFaceDown()).isTrue();
                    assertThat(permanent.getCard()).isSameAs(onlyCard);
                });
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(onlyCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsPermanentEvenWithAnEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareSpell(List.of());

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(target.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Unnerving Grasp");
    }

    @Test
    void doesNotManifestWhenChosenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card first = new Forest();
        Card second = new Mountain();
        prepareSpell(List.of(first, second));

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(target.getCard());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Unnerving Grasp");
    }

    @Test
    void canChooseSecondCardAndTurnManifestedCreatureFaceUp() {
        Card first = new Mountain();
        Card second = new GrizzlyBears();
        prepareSpell(List.of(first, second));

        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(second);
                    assertThat(permanent.isFaceDown()).isTrue();
                });
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(second);
                    assertThat(permanent.isFaceDown()).isFalse();
                });
    }
    @Test
    void triggersManifestDreadAbilitiesEvenWithAnEmptyLibrary() {
        harness.addToBattlefield(player1, new ParanormalAnalyst());
        prepareSpell(List.of());

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getCard()).isInstanceOf(ParanormalAnalyst.class);
            assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        });
    }
    private void prepareSpell(List<Card> library) {
        harness.setHand(player1, List.of(new UnnervingGrasp()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
