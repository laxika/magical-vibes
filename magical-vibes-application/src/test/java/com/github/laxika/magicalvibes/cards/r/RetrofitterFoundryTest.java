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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RetrofitterFoundry.class)
class RetrofitterFoundryTest extends BaseCardTest {

    @Test
    @DisplayName("The untap ability untaps Retrofitter Foundry")
    void untapsFoundry() {
        Permanent foundry = addFoundryReady();
        foundry.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, indexOf(foundry), 0, null, null);
        harness.passBothPriorities();

        assertThat(foundry.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The Servo ability creates a colorless Servo artifact creature token")
    void createsServo() {
        Permanent foundry = addFoundryReady();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(foundry), 1, null, null);
        harness.passBothPriorities();

        Permanent servo = findPermanent(player1, "Servo");
        assertThat(servo.getCard().getPower()).isEqualTo(1);
        assertThat(servo.getCard().getToughness()).isEqualTo(1);
        assertThat(servo.getCard().getColor()).isNull();
        assertThat(servo.getCard().getSubtypes()).contains(CardSubtype.SERVO);
        assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.hasKeyword(gd, servo, Keyword.FLYING)).isFalse();
        assertThat(foundry.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing a Servo creates a Thopter, then sacrificing it creates a Construct")
    void upgradesServoToThopterToConstruct() {
        Permanent foundry = addFoundryReady();
        createServo(foundry);

        untapFoundry(foundry);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(foundry), 2, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Servo")).isEmpty();
        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(thopter.getCard().getSubtypes()).contains(CardSubtype.THOPTER);
        assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();

        untapFoundry(foundry);
        harness.activateAbility(player1, indexOf(foundry), 3, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thopter")).isEmpty();
        Permanent construct = findPermanent(player1, "Construct");
        assertThat(construct.getCard().getPower()).isEqualTo(4);
        assertThat(construct.getCard().getToughness()).isEqualTo(4);
        assertThat(construct.getCard().getSubtypes()).contains(CardSubtype.CONSTRUCT);
        assertThat(construct.getCard().hasType(CardType.ARTIFACT)).isTrue();
    }

    private Permanent addFoundryReady() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new RetrofitterFoundry());
        foundry.setSummoningSick(false);
        return foundry;
    }

    private void createServo(Permanent foundry) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(foundry), 1, null, null);
        harness.passBothPriorities();
    }

    private void untapFoundry(Permanent foundry) {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(foundry), 0, null, null);
        harness.passBothPriorities();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    @Test
    @DisplayName("The Servo ability creates a 1/1 colorless artifact creature token")
    void createsServoToken() {
        Permanent foundry = addFoundryReady();
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
    @DisplayName("Sacrificing a Servo creates a Thopter")
    void sacrificesServoForThopter() {
        Permanent foundry = addFoundryReady();
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
        Permanent foundry = addFoundryReady();
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
}
