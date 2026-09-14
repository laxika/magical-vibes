package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.SkywingAven;
import com.github.laxika.magicalvibes.cards.s.StupefyingTouch;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PossessedAven.class, SkywingAven.class, PardicLancer.class, StupefyingTouch.class})
class PossessedAvenTest extends BaseCardTest {

    @Test
    @DisplayName("Threshold gives Possessed Aven +1/+1, makes it black, and grants its ability")
    void thresholdChangesCreatureAndGrantsAbility() {
        fillGraveyard(player1, 7);
        Permanent aven = addReadyAven();

        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aven)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, aven)).containsExactly(CardColor.BLACK);
        assertThat(gs.getEffectiveActivatedAbilities(gd, aven)).hasSize(1);
    }

    @Test
    @DisplayName("Threshold counts only cards in Possessed Aven's controller's graveyard")
    void thresholdUsesControllerGraveyard() {
        fillGraveyard(player1, 6);
        fillGraveyard(player2, 7);
        Permanent aven = addReadyAven();

        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aven)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, aven)).doesNotContain(CardColor.BLACK);
        assertThat(gs.getEffectiveActivatedAbilities(gd, aven)).isEmpty();
    }

    @Test
    @DisplayName("Threshold ability destroys a target blue creature")
    void abilityDestroysBlueCreature() {
        fillGraveyard(player1, 7);
        Permanent aven = addReadyAven();
        Permanent target = addCreatureReady(player2, new SkywingAven());

        prepareActivation();
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(aven.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Skywing Aven");
        harness.assertInGraveyard(player2, "Skywing Aven");
    }

    @Test
    @DisplayName("Threshold ability can destroy a blue creature you control")
    void abilityDestroysOwnBlueCreature() {
        fillGraveyard(player1, 7);
        Permanent aven = addReadyAven();
        Permanent target = addCreatureReady(player1, new SkywingAven());

        prepareActivation();
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(aven.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Skywing Aven");
        harness.assertInGraveyard(player1, "Skywing Aven");
    }

    @Test
    @DisplayName("Threshold ability cannot target a nonblue creature")
    void abilityCannotTargetNonblueCreature() {
        fillGraveyard(player1, 7);
        addReadyAven();
        Permanent target = addCreatureReady(player2, new PardicLancer());

        prepareActivation();
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blue creature");
    }

    @Test
    @DisplayName("Threshold ability cannot target a blue noncreature permanent")
    void abilityCannotTargetBlueNoncreaturePermanent() {
        fillGraveyard(player1, 7);
        addReadyAven();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StupefyingTouch());

        prepareActivation();
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blue creature");
    }

    @Test
    @DisplayName("Threshold bonuses and ability disappear below seven cards in the graveyard")
    void thresholdDisappearsBelowSevenCards() {
        fillGraveyard(player1, 7);
        Permanent aven = addReadyAven();
        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aven)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, aven)).doesNotContain(CardColor.BLACK);
        assertThat(gs.getEffectiveActivatedAbilities(gd, aven)).isEmpty();
    }

    private Permanent addReadyAven() {
        return addCreatureReady(player1, new PossessedAven());
    }

    private void prepareActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new PossessedAven());
        }
        harness.setGraveyard(player, cards);
    }
}
