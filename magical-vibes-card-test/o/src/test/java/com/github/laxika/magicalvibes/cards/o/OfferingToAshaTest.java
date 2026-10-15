package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzledLeotau;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OfferingToAsha.class, GrizzledLeotau.class})
class OfferingToAshaTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the spell and gains 4 life when the controller cannot pay {4}")
    void countersAndGainsLifeWhenOpponentCannotPay() {
        GrizzledLeotau leotau = new GrizzledLeotau();
        harness.setHand(player1, List.of(leotau));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player2, List.of(new OfferingToAsha()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, leotau.getId());

        harness.assertInGraveyard(player1, "Grizzled Leotau");
        harness.assertNotOnBattlefield(player1, "Grizzled Leotau");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("Spell resolves but controller still gains 4 life when the opponent pays {4}")
    void gainsLifeButSpellNotCounteredWhenOpponentPays() {
        GrizzledLeotau leotau = new GrizzledLeotau();
        harness.setHand(player1, List.of(leotau));
        harness.addMana(player1, ManaColor.GREEN, 5); // 1 to cast, 4 to pay
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player2, List.of(new OfferingToAsha()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, leotau.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // Offering to Asha's controller gains life regardless of whether the spell is countered.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 4);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzled Leotau");
    }

    @Test
    @DisplayName("Counters the spell and gains life when its controller declines an affordable payment")
    void countersWhenOpponentDeclinesPayment() {
        GrizzledLeotau leotau = new GrizzledLeotau();
        harness.setHand(player1, List.of(leotau));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new OfferingToAsha()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, leotau.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzled Leotau");
        harness.assertNotOnBattlefield(player1, "Grizzled Leotau");
        harness.assertLife(player2, 24);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not gain life when the target spell has already left the stack")
    void doesNotGainLifeWhenTargetBecomesIllegal() {
        GrizzledLeotau leotau = new GrizzledLeotau();
        harness.setHand(player1, List.of(leotau));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new OfferingToAsha(), new OfferingToAsha()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, leotau.getId());
        harness.castAndResolveInstant(player2, 0, leotau.getId());
        harness.assertLife(player2, 24);

        harness.passBothPriorities();

        harness.assertLife(player2, 24);
        harness.assertInGraveyard(player1, "Grizzled Leotau");
        harness.assertNotOnBattlefield(player1, "Grizzled Leotau");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
