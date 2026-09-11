package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.RED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CharredFoyerWarpedSpace.class, Opt.class})
class CharredFoyerWarpedSpaceTest extends BaseCardTest {

    @Test
    void charredFoyerExilesTheTopCardAtTheBeginningOfUpkeep() {
        castRoom(0);
        Opt topCard = new Opt();
        harness.setLibrary(player1, List.of(topCard));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    void warpedSpaceLetsTheControllerCastOneExiledSpellForFreeEachTurn() {
        castRoom(1);
        Opt firstSpell = new Opt();
        Opt secondSpell = new Opt();
        harness.setExile(player1, List.of(firstSpell, secondSpell));
        gd.exilePlayPermissions.put(firstSpell.getId(), player1.getId());
        gd.exilePlayPermissions.put(secondSpell.getId(), player1.getId());

        harness.castFromExile(player1, firstSpell.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThatThrownBy(() -> harness.castFromExile(player1, secondSpell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void warpedSpaceDoesNotApplyWhileItsDoorIsLocked() {
        castRoom(0);
        Opt spell = new Opt();
        harness.setExile(player1, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new CharredFoyerWarpedSpace()));
        harness.addMana(player1, RED, doorIndex == 0 ? 4 : 6);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
    }
}
