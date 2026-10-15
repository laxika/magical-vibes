package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConscriptedInfantry.class, Shock.class})
class ConscriptedInfantryTest extends BaseCardTest {

    @Test
    @DisplayName("When Conscripted Infantry dies, it creates a 1/1 colorless Soldier artifact creature token")
    void createsSoldierTokenWhenItDies() {
        harness.addToBattlefield(player1, new ConscriptedInfantry());

        killWithShock(player2, player1, "Conscripted Infantry");
        harness.passBothPriorities();

        Permanent soldier = findPermanent(player1, "Soldier");

        assertThat(soldier.getCard().getName()).isEqualTo("Soldier");
        assertThat(soldier.getCard().getPower()).isEqualTo(1);
        assertThat(soldier.getCard().getToughness()).isEqualTo(1);
        assertThat(soldier.getCard().getColors()).isEmpty();
        assertThat(soldier.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
        assertThat(soldier.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(soldier.getCard().hasType(CardType.CREATURE)).isTrue();
    }

    @Test
    @DisplayName("Each Infantry dying simultaneously creates exactly one Soldier")
    void simultaneousDeathsCreateOneTokenEach() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ConscriptedInfantry());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ConscriptedInfantry());
        first.setMarkedDamage(1);
        second.setMarkedDamage(1);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Conscripted Infantry")).isZero();
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(2);
        assertThat(countPermanents(player2, "Soldier")).isZero();
    }

    @Test
    @DisplayName("An opponent's Infantry creates a Soldier for that opponent")
    void opponentReceivesTheirDeathToken() {
        harness.addToBattlefield(player2, new ConscriptedInfantry());

        killWithShock(player1, player2, "Conscripted Infantry");
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Soldier")).isEqualTo(1);
        assertThat(countPermanents(player1, "Soldier")).isZero();
        harness.assertInGraveyard(player2, "Conscripted Infantry");
    }

    @Test
    @DisplayName("The created Soldier does not inherit Infantry's death ability")
    void soldierDeathDoesNotCreateAnotherSoldier() {
        harness.addToBattlefield(player1, new ConscriptedInfantry());
        killWithShock(player2, player1, "Conscripted Infantry");
        resolveAllTriggers();

        findPermanent(player1, "Soldier").setMarkedDamage(1);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Soldier")).isZero();
        assertThat(countPermanents(player2, "Soldier")).isZero();
    }

    private void killWithShock(Player caster, Player targetController, String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
