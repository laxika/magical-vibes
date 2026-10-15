package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MysticSanctuary;
import com.github.laxika.magicalvibes.cards.u.UsherToSafety;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShepherdOfTheFlock.class, UsherToSafety.class, MysticSanctuary.class})
class ShepherdOfTheFlockTest extends BaseCardTest {

    @Test
    void adventureReturnsControlledPermanentAndExilesTheCardWithCreatureCastPermission() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ShepherdOfTheFlock());
        ShepherdOfTheFlock card = new ShepherdOfTheFlock();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).contains(target.getCard());
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNotNull();
        assertThat(harness.getGameData().exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetPermanentControlledByOpponent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShepherdOfTheFlock());
        ShepherdOfTheFlock card = new ShepherdOfTheFlock();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void adventureCanReturnALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MysticSanctuary());
        harness.setHand(player1, List.of(new ShepherdOfTheFlock()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAdventure(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerHands.get(player1.getId())).contains(land.getCard());
    }

    @Test
    void adventureReturnsBorrowedPermanentToItsOwner() {
        ShepherdOfTheFlock borrowed = new ShepherdOfTheFlock();
        borrowed.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, borrowed);
        harness.setHand(player1, List.of(new ShepherdOfTheFlock()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player2.getId())).contains(borrowed);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(borrowed);
    }

    @Test
    void creatureCanBeCastFromExileAfterAdventureResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ShepherdOfTheFlock());
        ShepherdOfTheFlock card = new ShepherdOfTheFlock();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(card.getId()));
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void adventureWithTargetThatChangesControllerGoesToGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ShepherdOfTheFlock());
        ShepherdOfTheFlock card = new ShepherdOfTheFlock();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAdventure(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }
}
