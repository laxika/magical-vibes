package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PassengerFerry.class, GrizzlyBears.class})
class PassengerFerryTest extends BaseCardTest {

    @Test
    void crewAnimatesFerryAndTapsCrew() {
        Permanent ferry = addFerryReady();
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ferry)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void payingBlueMakesAnotherAttackingCreatureUnblockable() {
        Permanent ferry = addFerryReady();
        addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        attacker.untap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0, 2));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(attacker.isCantBeBlocked()).isTrue();
        assertThat(ferry.isCantBeBlocked()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void decliningBlueDoesNotMakeAttackerUnblockable() {
        addFerryReady();
        addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        attacker.untap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0, 2));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(attacker.isCantBeBlocked()).isFalse();
    }

    @Test
    void attackTriggerCannotTargetTheFerryItself() {
        Permanent ferry = addFerryReady();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).get(2).tap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).get(2).untap();

        declareAttackers(player1, List.of(0, 2));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ferry.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addFerryReady() {
        Permanent ferry = new Permanent(new PassengerFerry());
        ferry.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ferry);
        return ferry;
    }
}
