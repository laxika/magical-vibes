package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FinalDeath;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Nylea's Huntmaster")
@CardUsed({NyleasHuntmaster.class, GrizzlyBears.class, FinalDeath.class})
class NyleasHuntmasterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives a creature +X/+0 equal to its controller's green devotion")
    void etbBoostsTargetByGreenDevotionIncludingItself() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NyleasHuntmaster()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's green devotion is not counted")
    void doesNotCountOpponentsGreenDevotion() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NyleasHuntmaster()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NyleasHuntmaster()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NyleasHuntmaster()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.getGameService().playCard(
                gd, player1, 0, 0, opponentCreature.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Can target itself when it enters an otherwise empty battlefield")
    void canTargetItself() {
        harness.setHand(player1, List.of(new NyleasHuntmaster()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent huntmaster = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handlePermanentChosen(player1, huntmaster.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, huntmaster)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, huntmaster)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts green permanents added after the ability triggers")
    void countsDevotionAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NyleasHuntmaster());
        harness.enterBattlefieldAndReturn(player1, new NyleasHuntmaster());
        harness.handlePermanentChosen(player1, target.getId());
        harness.addToBattlefield(player1, new NyleasHuntmaster());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability resolves after its source leaves and excludes that source from devotion")
    void resolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NyleasHuntmaster());
        Permanent source = harness.enterBattlefieldAndReturn(player1, new NyleasHuntmaster());
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player2, List.of(new FinalDeath()));
        harness.addMana(player2, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("The resolved boost stays fixed when devotion later increases or decreases")
    void resolvedBoostDoesNotTrackDevotion() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NyleasHuntmaster());
        Permanent source = harness.enterBattlefieldAndReturn(player1, new NyleasHuntmaster());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);

        Permanent extra = harness.addToBattlefieldAndReturn(player1, new NyleasHuntmaster());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);

        harness.setHand(player2, List.of(new FinalDeath(), new FinalDeath()));
        harness.addMana(player2, ManaColor.BLACK, 10);
        harness.castAndResolveInstant(player2, 0, extra.getId());
        harness.castAndResolveInstant(player2, 0, source.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source, extra);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }
}
