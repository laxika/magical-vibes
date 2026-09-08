package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlaneswalkersMischiefTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a random instant or sorcery from the target opponent's hand")
    void exilesInstantOrSorceryFromOpponentsHand() {
        addMischief();
        Card shock = new Shock();
        harness.setHand(player2, List.of(shock));
        addAbilityMana();

        activateMischief();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(shock);
    }

    @Test
    @DisplayName("Leaves a randomly revealed non-instant and non-sorcery in hand")
    void leavesNonSpellInHand() {
        addMischief();
        Card forest = new Forest();
        harness.setHand(player2, List.of(forest));
        addAbilityMana();

        activateMischief();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(forest);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Lets its controller cast the exiled spell without paying its mana cost")
    void castsExiledSpellForFree() {
        addMischief();
        Card shock = new Shock();
        harness.setHand(player2, List.of(shock));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addAbilityMana();

        activateMischief();
        gs.playCardFromExile(gd, player1, shock.getId(), null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(shock);
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(
                permanent -> permanent.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Returns an uncast exiled spell to its owner's hand at the next end step")
    void returnsUncastSpellAtNextEndStep() {
        addMischief();
        Card shock = new Shock();
        harness.setHand(player2, List.of(shock));
        addAbilityMana();

        activateMischief();
        harness.inMutationScope(() -> stepTriggerService().handleEndStepTriggers(gd));

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(shock);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(shock);
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetController() {
        addMischief();
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addMischief() {
        return harness.addToBattlefieldAndReturn(player1, new PlaneswalkersMischief());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void activateMischief() {
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
    }

    private StepTriggerService stepTriggerService() {
        return GameTestEngineContext.get().getBean(StepTriggerService.class);
    }
}
