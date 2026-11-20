package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodmistInfiltrator.class, SauroformHybrid.class})
class BloodmistInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers sacrificing another creature")
    void attackingOffersSacrifice() {
        Permanent infiltrator = addCreatureReady(player1, new BloodmistInfiltrator());
        Permanent hybrid = addCreatureReady(player1, new SauroformHybrid());
        Permanent otherHybrid = addCreatureReady(player1, new SauroformHybrid());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(hybrid.getId(), otherHybrid.getId());
        assertThat(choice.validIds()).doesNotContain(infiltrator.getId());
    }

    @Test
    @DisplayName("Sacrificing another creature makes Bloodmist Infiltrator unblockable")
    void sacrificingAnotherCreatureMakesItUnblockable() {
        Permanent infiltrator = addCreatureReady(player1, new BloodmistInfiltrator());
        Permanent hybrid = addCreatureReady(player1, new SauroformHybrid());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gqs.hasCantBeBlocked(gd, infiltrator)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(hybrid.getCard());
    }

    @Test
    @DisplayName("Declining the sacrifice does not make Bloodmist Infiltrator unblockable")
    void decliningSacrificeDoesNothing() {
        Permanent infiltrator = addCreatureReady(player1, new BloodmistInfiltrator());
        Permanent hybrid = addCreatureReady(player1, new SauroformHybrid());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasCantBeBlocked(gd, infiltrator)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hybrid);
    }

    @Test
    @DisplayName("The unblockable effect expires at end of turn")
    void unblockableExpiresAtEndOfTurn() {
        Permanent infiltrator = addCreatureReady(player1, new BloodmistInfiltrator());
        addCreatureReady(player1, new SauroformHybrid());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gqs.hasCantBeBlocked(gd, infiltrator)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasCantBeBlocked(gd, infiltrator)).isFalse();
    }

    @Test
    @DisplayName("With no other creature, the sacrifice isn't offered")
    void noOtherCreatureDoesNothing() {
        Permanent infiltrator = addCreatureReady(player1, new BloodmistInfiltrator());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        assertThat(gqs.hasCantBeBlocked(gd, infiltrator)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing makes the attacker unblockable without a separate trigger")
    void sacrificeFollowUpResolvesWithAttackTrigger() {
        Permanent infiltrator = addCreatureReady(player1, new BloodmistInfiltrator());
        Permanent sacrifice = addCreatureReady(player1, new BloodmistInfiltrator());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gqs.hasCantBeBlocked(gd, infiltrator)).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
    }

    @Test
    @DisplayName("Another Bloodmist Infiltrator can be sacrificed, but an opponent's cannot")
    void sacrificeChoiceUsesPermanentIdentityAndController() {
        Permanent infiltrator = addCreatureReady(player1, new BloodmistInfiltrator());
        Permanent another = addCreatureReady(player1, new BloodmistInfiltrator());
        Permanent opponent = addCreatureReady(player2, new BloodmistInfiltrator());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(another.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(infiltrator);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponent);
        assertThat(gqs.hasCantBeBlocked(gd, infiltrator)).isTrue();
    }
}
