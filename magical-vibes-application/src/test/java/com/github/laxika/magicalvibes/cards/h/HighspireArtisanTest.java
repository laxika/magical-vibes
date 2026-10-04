package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.SelectForInspection;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HighspireArtisan.class, SelectForInspection.class, WindDrake.class})
class HighspireArtisanTest extends BaseCardTest {

    @Test
    @DisplayName("Fabricate mode puts a +1/+1 counter on Highspire Artisan")
    void fabricateCountersMode() {
        castArtisan(0);
        resolveAllTriggers();

        Permanent artisan = findPermanent(player1, "Highspire Artisan");
        assertThat(artisan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(4);
    }

    @Test
    @DisplayName("Fabricate mode creates a 1/1 colorless Servo artifact creature token")
    void fabricateServoMode() {
        castArtisan(1);
        resolveAllTriggers();

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
    @DisplayName("Fabricate asks whether to place counters only when the trigger resolves")
    void choosesCountersDuringTriggerResolution() {
        harness.setHand(player1, List.of(new HighspireArtisan()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent artisan = findPermanent(player1, "Highspire Artisan");
        assertThat(artisan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(artisan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Fabricate creates a Servo when Artisan leaves before the trigger resolves")
    void createsServoWhenSourceLeavesBeforeResolution() {
        harness.setHand(player2, List.of(new SelectForInspection()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player2, ManaColor.BLUE, 1);
        castArtisan(0);
        harness.passBothPriorities();

        Permanent artisan = findPermanent(player1, "Highspire Artisan");
        artisan.tap();
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, artisan.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Highspire Artisan");
        harness.assertNotOnBattlefield(player1, "Highspire Artisan");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Servo")).hasSize(1);
        assertThat(findPermanents(player2, "Servo")).isEmpty();
    }

    @Test
    @DisplayName("Reach allows Artisan to block a flying creature")
    void blocksFlyingCreature() {
        Permanent artisan = harness.addToBattlefieldAndReturn(player2, new HighspireArtisan());
        addCreatureReady(player1, new WindDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(artisan.isBlocking()).isTrue();
        resolveCombat();
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Highspire Artisan");
    }

    private void castArtisan(int mode) {
        harness.setHand(player1, List.of(new HighspireArtisan()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0, mode);
    }
}
