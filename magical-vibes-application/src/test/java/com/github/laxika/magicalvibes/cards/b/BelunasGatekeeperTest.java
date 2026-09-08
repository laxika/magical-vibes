package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EntryDenied;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BelunasGatekeeper.class, EntryDenied.class, GrizzlyBears.class, Island.class, AirElemental.class})
class BelunasGatekeeperTest extends BaseCardTest {

    @Test
    void adventureReturnsEligibleCreatureAndExilesTheCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        BelunasGatekeeper card = new BelunasGatekeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetAnIneligiblePermanent() {
        Permanent largeCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        BelunasGatekeeper card = new BelunasGatekeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, largeCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 3 or less");
    }

    @Test
    void adventureCannotTargetACreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        BelunasGatekeeper card = new BelunasGatekeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you don't control");
    }

    @Test
    void adventureCannotTargetALand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        BelunasGatekeeper card = new BelunasGatekeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you don't control");
    }
}
