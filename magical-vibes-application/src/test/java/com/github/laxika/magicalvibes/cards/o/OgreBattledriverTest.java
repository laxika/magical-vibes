package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PincherBeetles;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OgreBattledriver.class, GrizzlyBears.class, FugitiveWizard.class, PincherBeetles.class, Disperse.class})
class OgreBattledriverTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature you control entering gets +2/+0 and haste")
    void boostsAndHastesEnteringCreature() {
        harness.addToBattlefield(player1, new OgreBattledriver());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities(); // resolve the creature spell -> it enters, Battledriver triggers
        harness.passBothPriorities(); // resolve the trigger

        assertThat(gd.stack).isEmpty();
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The boost and haste wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new OgreBattledriver());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger for a creature an opponent controls")
    void noTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new OgreBattledriver());
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new FugitiveWizard(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent wizard = findPermanent(player2, "Fugitive Wizard");
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(1);
        assertThat(wizard.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger for itself entering")
    void noTriggerForItself() {
        harness.castFromHand(player1, new OgreBattledriver(), "{2}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent ogre = findPermanent(player1, "Ogre Battledriver");
        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(3);
        assertThat(ogre.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The ability affects a creature with shroud because it does not target")
    void boostsEnteringCreatureWithShroud() {
        harness.addToBattlefield(player1, new OgreBattledriver());

        harness.castFromHand(player1, new PincherBeetles(), "{2}{G}");
        harness.passBothPriorities();
        Permanent beetles = findPermanent(player1, "Pincher Beetles");
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, beetles)).isEqualTo(3);
        assertThat(beetles.hasKeyword(Keyword.HASTE)).isFalse();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, beetles)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, beetles)).isEqualTo(1);
        assertThat(beetles.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Multiple Battledrivers independently boost the entering creature")
    void multipleBattledriversStackTheirBoosts() {
        harness.addToBattlefield(player1, new OgreBattledriver());
        harness.addToBattlefield(player1, new OgreBattledriver());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The trigger still resolves after Battledriver leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreBattledriver());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, ogre.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ogre);

        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("An old trigger does not affect a creature that leaves and returns")
    void oldTriggerDoesNotBoostReturnedCreature() {
        harness.addToBattlefield(player1, new OgreBattledriver());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        Permanent originalBears = findPermanent(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, originalBears.getId());
        Permanent returnedBears = harness.enterBattlefieldAndReturn(player1, originalBears.getCard());
        assertThat(returnedBears.getId()).isNotEqualTo(originalBears.getId());

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, returnedBears)).isEqualTo(4);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, returnedBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returnedBears)).isEqualTo(2);
        assertThat(returnedBears.hasKeyword(Keyword.HASTE)).isTrue();
    }
}
