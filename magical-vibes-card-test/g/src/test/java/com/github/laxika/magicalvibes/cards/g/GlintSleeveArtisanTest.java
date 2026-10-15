package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ChandrasPyrohelix;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlintSleeveArtisan.class, ChandrasPyrohelix.class})
class GlintSleeveArtisanTest extends BaseCardTest {

    @Test
    @DisplayName("Fabricate mode puts a +1/+1 counter on Glint-Sleeve Artisan")
    void fabricateCountersMode() {
        castArtisan(0);
        resolveCreatureAndEtb();

        Permanent artisan = findPermanent(player1, "Glint-Sleeve Artisan");
        assertThat(artisan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(3);
    }

    @Test
    @DisplayName("Fabricate mode creates a 1/1 colorless Servo artifact creature token")
    void fabricateServoMode() {
        castArtisan(1);
        resolveCreatureAndEtb();

        List<Permanent> servos = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO))
                .toList();

        assertThat(servos).hasSize(1);
        Permanent servo = servos.getFirst();
        assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(1);
    }

    @Test
    @DisplayName("Fabricate waits until the trigger resolves to ask for the choice")
    void fabricateChoiceIsMadeOnResolution() {
        castArtisan(0);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        Permanent artisan = findPermanent(player1, "Glint-Sleeve Artisan");
        assertThat(artisan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(artisan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Fabricate creates a Servo if Artisan dies before the trigger resolves")
    void fabricateCreatesServoWhenSourceDies() {
        castArtisan(0);
        harness.passBothPriorities();
        Permanent artisan = findPermanent(player1, "Glint-Sleeve Artisan");

        harness.setHand(player2, List.of(new ChandrasPyrohelix()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, Map.of(artisan.getId(), 2));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Glint-Sleeve Artisan");

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO)))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO)))
                .isEmpty();
    }

    private void castArtisan(int mode) {
        harness.setHand(player1, List.of(new GlintSleeveArtisan()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, mode);
    }

    private void resolveCreatureAndEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
