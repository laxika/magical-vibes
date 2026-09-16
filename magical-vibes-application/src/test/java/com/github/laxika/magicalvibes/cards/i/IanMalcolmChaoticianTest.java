package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IanMalcolmChaotician.class, Divination.class, Forest.class, GoblinPiker.class})
class IanMalcolmChaoticianTest extends BaseCardTest {

    @Test
    void controllersSecondDrawExilesTheirLibraryTopAndOpponentCanCastItWithAnyMana() {
        Permanent ian = harness.addToBattlefieldAndReturn(player1, new IanMalcolmChaotician());
        Card exiledSpell = new GoblinPiker();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), exiledSpell));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(exiledSpell.getId()).sourcePermanentId()).isEqualTo(ian.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castFromExile(player2, exiledSpell.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Goblin Piker");
    }

    @Test
    void opponentSecondDrawExilesTheirTopCardButTheyCannotCastTheirOwnExile() {
        harness.addToBattlefield(player1, new IanMalcolmChaotician());
        Card exiledCard = new GoblinPiker();
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), exiledCard));
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(exiledCard.getId()).sourcePermanentId()).isNotNull();
        harness.addMana(player2, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castFromExile(player2, exiledCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
