package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OliviasAttendants.class, GrizzlyBears.class})
class OliviasAttendantsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Blood token for each combat damage dealt to a player")
    void combatDamageCreatesBloodTokens() {
        Permanent attendants = addReadyAttendants(3);
        attendants.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(findPermanents(player1, "Blood")).hasSize(3)
                .allSatisfy(blood -> {
                    assertThat(blood.getCard().isToken()).isTrue();
                    assertThat(blood.getCard().getType()).isEqualTo(CardType.ARTIFACT);
                    assertThat(blood.getCard().getSubtypes()).contains(CardSubtype.BLOOD);
                });
    }

    @Test
    @DisplayName("The activated ability deals damage to a creature and creates a Blood token")
    void activatedAbilityDamagesCreatureAndCreatesBlood() {
        addReadyAttendants(6);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }

    private Permanent addReadyAttendants(int power) {
        OliviasAttendants card = new OliviasAttendants();
        card.setPower(power);
        card.setToughness(6);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
