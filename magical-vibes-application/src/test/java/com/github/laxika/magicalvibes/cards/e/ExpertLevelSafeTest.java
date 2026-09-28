package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExpertLevelSafe.class, GrizzlyBears.class})
class ExpertLevelSafeTest extends BaseCardTest {

    @Test
    void entersByExilingTopTwoCardsFaceDownWithIt() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Permanent safe = castSafe(List.of(first, second));

        assertThat(gd.getCardsExiledByPermanent(safe.getId())).containsExactly(first, second);
        assertThat(gd.getExiledWithPermanentEntries(safe.getId(), safe.getCard().getId()))
                .allMatch(ExiledCardEntry::faceDown);
    }

    @Test
    void matchingChoicesSacrificeItAndReturnAllCardsExiledWithIt() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Permanent safe = castSafe(List.of(first, second));
        addManaForAbility();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleXValueChosen(player2, 2);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(safe);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(safe.getCard());
        assertThat(gd.getCardsExiledByPermanent(safe.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void nonmatchingChoicesExileOneMoreTopCardFaceDown() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Permanent safe = castSafe(List.of(first, second, third));
        addManaForAbility();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player2, 2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(safe);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(safe.getId())).containsExactly(first, second, third);
        assertThat(gd.getExiledWithPermanentEntries(safe.getId(), safe.getCard().getId()))
                .allMatch(ExiledCardEntry::faceDown);
    }

    @Test
    void activatedAbilityCannotTargetItsController() {
        harness.addToBattlefieldAndReturn(player1, new ExpertLevelSafe());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private Permanent castSafe(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new ExpertLevelSafe()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
