package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoftyDenial.class, AngelsMercy.class, CloudSprite.class})
class LoftyDenialTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when its controller declines to pay {1}")
    void countersWhenControllerDeclinesOneMana() {
        AngelsMercy angelsMercy = castTargetSpell();
        castLoftyDenial(angelsMercy);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Angel's Mercy");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Lets a spell resolve when its controller pays {1}")
    void resolvesWhenControllerPaysOneMana() {
        AngelsMercy angelsMercy = castTargetSpell();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        castLoftyDenial(angelsMercy);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Angel's Mercy");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Uses {4} when Lofty Denial's controller controls a creature with flying")
    void usesFourManaWithFlying() {
        harness.addToBattlefield(player2, new CloudSprite());
        AngelsMercy angelsMercy = castTargetSpell();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        castLoftyDenial(angelsMercy);

        harness.assertInGraveyard(player1, "Angel's Mercy");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Lets a spell resolve when its controller pays {4} with a flyer present")
    void resolvesWhenControllerPaysFourMana() {
        harness.addToBattlefield(player2, new CloudSprite());
        AngelsMercy angelsMercy = castTargetSpell();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        castLoftyDenial(angelsMercy);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Angel's Mercy");
        harness.assertLife(player1, 17);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Counters a spell when its controller declines to pay {4}")
    void countersWhenControllerDeclinesFourMana() {
        harness.addToBattlefield(player2, new CloudSprite());
        AngelsMercy angelsMercy = castTargetSpell();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        castLoftyDenial(angelsMercy);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Angel's Mercy");
        harness.assertLife(player1, 10);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's flying creature does not increase the payment")
    void opponentsFlyingCreatureDoesNotIncreasePayment() {
        harness.addToBattlefield(player1, new CloudSprite());
        AngelsMercy angelsMercy = castTargetSpell();
        castLoftyDenial(angelsMercy);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Checks for a flying creature at resolution rather than casting")
    void flyingCreatureEnteringBeforeResolutionIncreasesPayment() {
        AngelsMercy angelsMercy = castTargetSpell();
        harness.setHand(player2, List.of(new LoftyDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, angelsMercy.getId());
        harness.addToBattlefield(player2, new CloudSprite());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Angel's Mercy");
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Losing the last flying creature before resolution reduces the payment to {1}")
    void flyingCreatureLeavingBeforeResolutionReducesPayment() {
        harness.addToBattlefield(player2, new CloudSprite());
        AngelsMercy angelsMercy = castTargetSpell();
        harness.setHand(player2, List.of(new LoftyDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, angelsMercy.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private AngelsMercy castTargetSpell() {
        harness.setLife(player1, 10);
        AngelsMercy angelsMercy = new AngelsMercy();
        harness.setHand(player1, List.of(angelsMercy));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        return angelsMercy;
    }

    private void castLoftyDenial(AngelsMercy angelsMercy) {
        harness.setHand(player2, List.of(new LoftyDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, angelsMercy.getId());
    }
}
