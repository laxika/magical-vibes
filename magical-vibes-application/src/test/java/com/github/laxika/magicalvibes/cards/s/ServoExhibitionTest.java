package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ServoExhibition.class})
class ServoExhibitionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two 1/1 colorless Servo artifact creature tokens")
    void createsTwoServos() {
        harness.setHand(player1, List.of(new ServoExhibition()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> servos = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO))
                .toList();

        assertThat(servos).hasSize(2);
        assertThat(servos).allSatisfy(servo -> {
            assertThat(servo.getCard().isToken()).isTrue();
            assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(servo.getCard().getColor()).isNull();
            assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Each resolution creates two additional Servos for the caster")
    void repeatedResolutionsCreateAdditionalTokens() {
        harness.setHand(player1, List.of(new ServoExhibition(), new ServoExhibition()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4)
                .allSatisfy(servo -> {
                    assertThat(servo.getCard().isToken()).isTrue();
                    assertThat(servo.getCard().getSubtypes()).contains(CardSubtype.SERVO);
                });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }
}
