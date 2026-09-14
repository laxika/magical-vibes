package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.k.KrosanConstrictor;
import com.github.laxika.magicalvibes.cards.n.Narcissism;
import com.github.laxika.magicalvibes.cards.p.PardicLancer;
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

@CardUsed({PossessedCentaur.class, KrosanConstrictor.class, PardicLancer.class, Narcissism.class})
class PossessedCentaurTest extends BaseCardTest {

    @Test
    @DisplayName("Threshold gives Possessed Centaur +1/+1, makes it black, and grants its ability")
    void thresholdChangesCreatureAndGrantsAbility() {
        fillGraveyard(player1, 7);
        Permanent centaur = addReadyCentaur();

        assertThat(gqs.getEffectivePower(gd, centaur)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, centaur)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, centaur)).containsExactly(CardColor.BLACK);
        assertThat(gs.getEffectiveActivatedAbilities(gd, centaur)).hasSize(1);
    }

    @Test
    @DisplayName("Threshold counts only cards in Possessed Centaur's controller's graveyard")
    void thresholdUsesControllerGraveyard() {
        fillGraveyard(player1, 6);
        fillGraveyard(player2, 7);
        Permanent centaur = addReadyCentaur();

        assertThat(gqs.getEffectivePower(gd, centaur)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, centaur)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, centaur)).doesNotContain(CardColor.BLACK);
        assertThat(gs.getEffectiveActivatedAbilities(gd, centaur)).isEmpty();
    }

    @Test
    @DisplayName("Threshold ability destroys a target green creature")
    void abilityDestroysGreenCreature() {
        fillGraveyard(player1, 7);
        Permanent centaur = addReadyCentaur();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KrosanConstrictor());

        prepareActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(centaur.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Krosan Constrictor");
        harness.assertInGraveyard(player2, "Krosan Constrictor");
    }

    @Test
    @DisplayName("Threshold ability cannot target a nongreen creature")
    void abilityCannotTargetNongreenCreature() {
        fillGraveyard(player1, 7);
        addReadyCentaur();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PardicLancer());

        prepareActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("green creature");
    }

    @Test
    @DisplayName("Threshold ability cannot target a green noncreature permanent")
    void abilityCannotTargetGreenNoncreaturePermanent() {
        fillGraveyard(player1, 7);
        addReadyCentaur();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Narcissism());

        prepareActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("green creature");
    }

    @Test
    @DisplayName("An activated threshold ability resolves after threshold is lost")
    void activatedAbilityResolvesAfterThresholdIsLost() {
        fillGraveyard(player1, 7);
        Permanent centaur = addReadyCentaur();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KrosanConstrictor());

        prepareActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertThat(gs.getEffectiveActivatedAbilities(gd, centaur)).isEmpty();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Krosan Constrictor");
        harness.assertInGraveyard(player2, "Krosan Constrictor");
    }

    @Test
    @DisplayName("Threshold bonuses and ability disappear below seven cards in the graveyard")
    void thresholdDisappearsBelowSevenCards() {
        fillGraveyard(player1, 7);
        Permanent centaur = addReadyCentaur();
        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, centaur)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, centaur)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, centaur)).doesNotContain(CardColor.BLACK);
        assertThat(gs.getEffectiveActivatedAbilities(gd, centaur)).isEmpty();
    }

    private Permanent addReadyCentaur() {
        return addCreatureReady(player1, new PossessedCentaur());
    }

    private void prepareActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new PardicLancer());
        }
        harness.setGraveyard(player, cards);
    }
}
