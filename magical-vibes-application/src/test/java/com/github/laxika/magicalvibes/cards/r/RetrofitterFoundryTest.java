package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RetrofitterFoundry.class)
class RetrofitterFoundryTest extends BaseCardTest {

    @Test
    @DisplayName("The Servo ability creates a 1/1 colorless artifact creature token")
    void createsServoToken() {
        Permanent foundry = addFoundry();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(foundry), 1, null, null);
        harness.passBothPriorities();

        Permanent servo = findPermanent(player1, "Servo");
        assertThat(servo.getCard().getPower()).isEqualTo(1);
        assertThat(servo.getCard().getToughness()).isEqualTo(1);
        assertThat(servo.getCard().getColor()).isNull();
        assertThat(servo.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(servo.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(servo.getCard().getSubtypes()).contains(CardSubtype.SERVO);
        assertThat(foundry.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The untap ability untaps the Foundry")
    void untapsFoundry() {
        Permanent foundry = addFoundry();
        foundry.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, indexOf(foundry), 0, null, null);
        harness.passBothPriorities();

        assertThat(foundry.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sacrificing a Servo creates a Thopter")
    void sacrificesServoForThopter() {
        Permanent foundry = addFoundry();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, indexOf(foundry), 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(foundry), 0, null, null);
        harness.passBothPriorities();

        Permanent servo = findPermanent(player1, "Servo");
        harness.activateAbility(player1, indexOf(foundry), 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(servo);
        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(thopter.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(foundry.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing a Thopter creates a 4/4 Construct")
    void sacrificesThopterForConstruct() {
        Permanent foundry = addFoundry();
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.activateAbility(player1, indexOf(foundry), 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(foundry), 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(foundry), 2, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(foundry), 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(foundry), 3, null, null);
        harness.passBothPriorities();

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(construct.getCard().getPower()).isEqualTo(4);
        assertThat(construct.getCard().getToughness()).isEqualTo(4);
        assertThat(construct.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(construct.getCard().getSubtypes()).contains(CardSubtype.CONSTRUCT);
    }

    private Permanent addFoundry() {
        return harness.addToBattlefieldAndReturn(player1, new RetrofitterFoundry());
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
