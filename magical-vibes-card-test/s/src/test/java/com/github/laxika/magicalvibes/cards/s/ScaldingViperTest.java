package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RedcapThief;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScaldingViper.class, SteamClean.class, AirElemental.class, FountainOfYouth.class,
        GrizzlyBears.class, Island.class, RedcapThief.class})
class ScaldingViperTest extends BaseCardTest {

    @Test
    void adventureReturnsTargetNonlandPermanentAndExilesTheCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        ScaldingViper card = new ScaldingViper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInHand(player2, "Fountain of Youth");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetLand() {
        Island land = new Island();
        harness.addToBattlefield(player2, land);
        harness.setHand(player1, List.of(new ScaldingViper()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0,
                harness.getPermanentId(player2, "Island")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ScaldingViper card = new ScaldingViper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scalding Viper");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void dealsDamageToOpponentWhenTheyCastSpellWithManaValueThreeOrLess() {
        harness.addToBattlefield(player1, new ScaldingViper());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void doesNotTriggerForOpponentSpellWithManaValueGreaterThanThree() {
        harness.addToBattlefield(player1, new ScaldingViper());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new AirElemental(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void triggersForSpellWithManaValueExactlyThree() {
        harness.addToBattlefield(player1, new ScaldingViper());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new RedcapThief(), "{2}{R}");
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    void triggersForZeroManaValueArtifactSpell() {
        harness.addToBattlefield(player1, new ScaldingViper());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new FountainOfYouth(), "{0}");
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    void doesNotTriggerForControllersSpell() {
        harness.addToBattlefield(player1, new ScaldingViper());

        harness.castFromHand(player1, new ScaldingViper(), "{1}{R}");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void triggersForOpponentAdventureThatReturnsSource() {
        Permanent viper = harness.addToBattlefieldAndReturn(player1, new ScaldingViper());
        harness.setHand(player2, List.of(new ScaldingViper()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAdventure(player2, 0, viper.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertInHand(player1, "Scalding Viper");
        harness.assertNotOnBattlefield(player1, "Scalding Viper");
    }

    @Test
    void triggerStillDealsDamageWhenViperLeavesBeforeResolution() {
        Permanent viper = harness.addToBattlefieldAndReturn(player1, new ScaldingViper());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new ScaldingViper(), "{1}{R}");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, viper));
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    void adventureWithMissingTargetGoesToGraveyardWithoutExilePermission() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ScaldingViper card = new ScaldingViper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scalding Viper");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }
}
