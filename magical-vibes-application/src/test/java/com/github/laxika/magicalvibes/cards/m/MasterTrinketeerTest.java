package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MasterTrinketeerTest extends BaseCardTest {

    @Test
    void buffsOwnServosAndThoptersOnly() {
        harness.addToBattlefield(player1, new MasterTrinketeer());
        Card servo = creatureWithSubtype("Servo", CardSubtype.SERVO);
        Card thopter = creatureWithSubtype("Thopter", CardSubtype.THOPTER);
        Card bear = creatureWithSubtype("Bear", CardSubtype.BEAR);
        harness.addToBattlefield(player1, servo);
        harness.addToBattlefield(player1, thopter);
        harness.addToBattlefield(player1, bear);

        Card opponentThopter = creatureWithSubtype("Opponent Thopter", CardSubtype.THOPTER);
        harness.addToBattlefield(player2, opponentThopter);

        assertThat(powerOf(player1, "Servo")).isEqualTo(2);
        assertThat(toughnessOf(player1, "Servo")).isEqualTo(2);
        assertThat(powerOf(player1, "Thopter")).isEqualTo(2);
        assertThat(toughnessOf(player1, "Thopter")).isEqualTo(2);
        assertThat(powerOf(player1, "Bear")).isEqualTo(1);
        assertThat(toughnessOf(player1, "Bear")).isEqualTo(1);
        assertThat(powerOf(player2, "Opponent Thopter")).isEqualTo(1);
        assertThat(toughnessOf(player2, "Opponent Thopter")).isEqualTo(1);
    }

    @Test
    void createsBuffedServoToken() {
        harness.addToBattlefield(player1, new MasterTrinketeer());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        Permanent servo = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO))
                .findFirst()
                .orElseThrow();
        assertThat(servo.getCard().getPower()).isEqualTo(1);
        assertThat(servo.getCard().getToughness()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(2);
        assertThat(servo.getCard().getColor()).isNull();
        assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
    }

    @Test
    void abilityCanBeActivatedRepeatedly() {
        harness.addToBattlefield(player1, new MasterTrinketeer());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO))
                .count()).isEqualTo(2);
    }

    @Test
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new MasterTrinketeer());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Card creatureWithSubtype(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(subtype));
        card.setPower(1);
        card.setToughness(1);
        return card;
    }

    private int powerOf(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gqs.getEffectivePower(gd, findPermanent(player, name));
    }

    private int toughnessOf(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gqs.getEffectiveToughness(gd, findPermanent(player, name));
    }
}
