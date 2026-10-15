package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HopefulVigil.class})
class HopefulVigilTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a 2/2 white Knight token with vigilance")
    void entersCreatesKnightToken() {
        castAndResolve();

        assertThat(findPermanents(player1, "Knight"))
                .singleElement()
                .satisfies(knight -> {
                    assertThat(knight.getCard().isToken()).isTrue();
                    assertThat(knight.getCard().getPower()).isEqualTo(2);
                    assertThat(knight.getCard().getToughness()).isEqualTo(2);
                    assertThat(knight.getCard().getKeywords()).contains(Keyword.VIGILANCE);
                });
    }

    @Test
    @DisplayName("Scry 2 triggers when it is put into a graveyard from the battlefield")
    void scriesWhenPutIntoGraveyardFromBattlefield() {
        Permanent vigil = harness.addToBattlefieldAndReturn(player1, new HopefulVigil());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, vigil));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Paying {2}{W} sacrifices it")
    void sacrificeAbilitySacrificesIt() {
        castAndResolve();
        Permanent vigil = findPermanent(player1, "Hopeful Vigil");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vigil), 0, null, null);

        assertThat(findPermanents(player1, "Hopeful Vigil")).containsExactly(vigil);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Hopeful Vigil")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Hopeful Vigil");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("The graveyard trigger allows one card on top and one on the bottom")
    void scryReordersLibrary() {
        HopefulVigil first = new HopefulVigil();
        HopefulVigil second = new HopefulVigil();
        HopefulVigil third = new HopefulVigil();
        harness.setLibrary(player1, List.of(first, second, third));
        Permanent vigil = harness.addToBattlefieldAndReturn(player1, new HopefulVigil());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, vigil));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
    }

    @Test
    @DisplayName("The graveyard trigger resolves without a choice when the library is empty")
    void scryWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        Permanent vigil = harness.addToBattlefieldAndReturn(player1, new HopefulVigil());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, vigil));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(vigil.getCard());
    }

    private void castAndResolve() {
        harness.castFromHand(player1, new HopefulVigil(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
