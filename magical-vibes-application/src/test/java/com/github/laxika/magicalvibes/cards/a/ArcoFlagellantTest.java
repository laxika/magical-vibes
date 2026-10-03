package com.github.laxika.magicalvibes.cards.a;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ArcoFlagellant.class)
class ArcoFlagellantTest extends BaseCardTest {

    @Test
    @DisplayName("Squad creates one token copy for each additional payment")
    void squadCreatesTokenCopies() {
        harness.setHand(player1, List.of(new ArcoFlagellant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{2}", "{2}"));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Arco-Flagellant")).hasSize(3);
        assertThat(findPermanents(player1, "Arco-Flagellant"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    @DisplayName("Arco-Flagellant cannot block")
    void cannotBlock() {
        Permanent flagellant = harness.addToBattlefieldAndReturn(player2, new ArcoFlagellant());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ArcoFlagellant());
        attacker.setAttacking(true);

        assertThat(bls.canBlockAttacker(gd, flagellant, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Paying 3 life grants indestructible until end of turn")
    void payLifeGrantsIndestructibleUntilEndOfTurn() {
        Permanent flagellant = harness.addToBattlefieldAndReturn(player1, new ArcoFlagellant());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gqs.hasKeyword(gd, flagellant, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, flagellant, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate Endurant without 3 life")
    void cannotActivateWithoutEnoughLife() {
        harness.addToBattlefield(player1, new ArcoFlagellant());
        harness.setLife(player1, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Squad does not trigger when its additional cost was not paid")
    void unpaidSquadDoesNotTrigger() {
        harness.setHand(player1, List.of(new ArcoFlagellant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Arco-Flagellant")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Squad copies do not trigger unpaid squad and retain Endurant and the blocking restriction")
    void squadCopiesRetainAbilitiesWithoutTriggeringSquad() {
        harness.setHand(player1, List.of(new ArcoFlagellant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{2}"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Arco-Flagellant")).hasSize(2);
        Permanent original = findPermanent(player1, "Arco-Flagellant");
        Permanent token = findPermanents(player1, "Arco-Flagellant").stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new ArcoFlagellant());
        attacker.setAttacking(true);
        assertThat(bls.canBlockAttacker(gd, token, attacker,
                gd.playerBattlefields.get(player1.getId()))).isFalse();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(token), null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        assertThat(gqs.hasKeyword(gd, token, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, original, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Endurant pays life immediately but grants indestructible only when it resolves")
    void lifeIsPaidBeforeEndurantResolves() {
        Permanent flagellant = harness.addToBattlefieldAndReturn(player1, new ArcoFlagellant());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 17);
        assertThat(gqs.hasKeyword(gd, flagellant, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, flagellant, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertLife(player1, 17);
    }
}
