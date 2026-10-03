package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AzureBeastbinder.class, AirElemental.class, FugitiveWizard.class, GrizzlyBears.class,
        HillGiant.class, GideonBlackblade.class, JayemdaeTome.class})
class AzureBeastbinderTest extends BaseCardTest {

    @Test
    @DisplayName("Can't be blocked by creatures with power 2 or greater")
    void cantBeBlockedByCreaturesWithPowerAtLeastTwo() {
        Permanent beastbinder = addCreatureReady(player1, new AzureBeastbinder());
        Permanent wizard = addCreatureReady(player2, new FugitiveWizard());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent hillGiant = addCreatureReady(player2, new HillGiant());

        assertThat(bls.canBlockAttacker(gd, wizard, beastbinder,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
        assertThat(bls.canBlockAttacker(gd, bears, beastbinder,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, hillGiant, beastbinder,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Attacking makes an opposing creature lose abilities and become 2/2 until your next turn")
    void attackTriggerLastsUntilNextTurn() {
        addCreatureReady(player1, new AzureBeastbinder());
        Permanent elemental = addCreatureReady(player2, new AirElemental());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .containsExactlyInAnyOrder(elemental.getId(), player1.getId())
                .doesNotContain(player2.getId());

        harness.handlePermanentChosen(player1, elemental.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(2);

        resolveCombat();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(2);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);
    }

    @Test
    void mayChooseNoTargetWithoutChangingOpposingCreature() {
        Permanent beastbinder = addCreatureReady(player1, new AzureBeastbinder());
        Permanent elemental = addCreatureReady(player2, new AirElemental());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(beastbinder.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);
    }

    @Test
    void attackTriggerRemovesNoncreatureArtifactActivatedAbility() {
        addCreatureReady(player1, new AzureBeastbinder());
        Permanent tome = harness.addToBattlefieldAndReturn(player2, new JayemdaeTome());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, tome.getId());
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.isCreature(gd, tome)).isFalse();
    }

    @Test
    void noncreaturePlaneswalkerDoesNotBecomeTwoTwoOnItsControllersTurn() {
        addCreatureReady(player1, new AzureBeastbinder());
        Permanent gideon = harness.addToBattlefieldAndReturn(player2, new GideonBlackblade());
        gideon.setCounterCount(CounterType.LOYALTY, 4);
        assertThat(gqs.isCreature(gd, gideon)).isFalse();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, gideon.getId());
        resolveAllTriggers();
        resolveCombat();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gqs.isCreature(gd, gideon)).isTrue();
        assertThat(gqs.getEffectivePower(gd, gideon)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gideon)).isEqualTo(4);
    }

    @Test
    void countersApplyAboveBaseStatsAndEffectSurvivesSourceLeaving() {
        Permanent beastbinder = addCreatureReady(player1, new AzureBeastbinder());
        Permanent elemental = addCreatureReady(player2, new AirElemental());
        elemental.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, elemental.getId());
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(beastbinder);

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
    }
}
