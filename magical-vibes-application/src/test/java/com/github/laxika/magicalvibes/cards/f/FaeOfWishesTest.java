package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.Granted;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaeOfWishes.class, Granted.class, GrizzlyBears.class, Island.class, Mountain.class})
class FaeOfWishesTest extends BaseCardTest {

    @Test
    void adventureRevealsAndPutsNoncreatureSideboardCardIntoHand() {
        Card chosen = new Island();
        Card ineligible = new GrizzlyBears();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(chosen, ineligible)));
        FaeOfWishes fae = new FaeOfWishes();
        harness.setHand(player1, List.of(fae));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().cards()).containsExactly(chosen);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(ineligible);
        assertThat(gd.findExiledCard(fae.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(fae.getId())).isEqualTo(player1.getId());
    }

    @Test
    void activatedAbilityDiscardsTwoCardsAndReturnsFaeToHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        FaeOfWishes fae = new FaeOfWishes();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, fae);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(permanent);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(fae);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void adventureMayBeDeclinedEvenWithAnEligibleCard() {
        Card available = new Island();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(available)));
        FaeOfWishes fae = new FaeOfWishes();
        harness.setHand(player1, List.of(fae));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(available);
        assertThat(gd.findExiledCard(fae.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(fae.getId())).isEqualTo(player1.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void adventureWithOnlyCreatureCardsStillAllowsCastingFaeFromExile() {
        Card ineligible = new FaeOfWishes();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(ineligible)));
        FaeOfWishes fae = new FaeOfWishes();
        harness.setHand(player1, List.of(fae));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(ineligible);
        assertThat(gd.findExiledCard(fae.getId())).isNotNull();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, fae.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(fae.getId()));
        assertThat(gd.findExiledCard(fae.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(fae.getId());
    }

    @Test
    void activatedAbilityCannotBePaidWithOnlyOneCard() {
        FaeOfWishes fae = new FaeOfWishes();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, fae);
        Card available = new Island();
        harness.setHand(player1, List.of(available));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(permanent);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(available);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedSummoningSickFaePaysDiscardsBeforeReturningToItsOwner() {
        FaeOfWishes fae = new FaeOfWishes();
        fae.setOwnerId(player2.getId());
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, fae);
        gd.stolenCreatures.put(permanent.getId(), player2.getId());
        permanent.setTapped(true);
        permanent.setSummoningSick(true);
        Card first = new Island();
        Card second = new Mountain();
        harness.setHand(player1, List.of(first, second));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(permanent);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(permanent);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(fae);
    }
}
