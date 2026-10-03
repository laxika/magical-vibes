package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FieryImpulse;
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

@CardUsed({AspiringAeronaut.class, FieryImpulse.class})
class AspiringAeronautTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 1/1 colorless Thopter artifact creature token with flying")
    void etbCreatesThopterToken() {
        harness.setHand(player1, List.of(new AspiringAeronaut()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent thopter = findPermanent(player1, "Thopter");

        assertThat(thopter.getCard().isToken()).isTrue();
        assertThat(thopter.getCard().getName()).isEqualTo("Thopter");
        assertThat(thopter.getCard().getPower()).isEqualTo(1);
        assertThat(thopter.getCard().getToughness()).isEqualTo(1);
        assertThat(thopter.getCard().getColors()).isEmpty();
        assertThat(thopter.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Thopter is created only when the ETB trigger resolves and belongs to its controller")
    void tokenCreationUsesTheStackAndCreatesOneArtifactCreature() {
        harness.setHand(player1, List.of(new AspiringAeronaut()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        assertThat(countPermanents(player1, "Thopter")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Aspiring Aeronaut")).isEqualTo(1);
        assertThat(countPermanents(player1, "Thopter")).isZero();

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Thopter")).isEqualTo(1);
        assertThat(countPermanents(player2, "Thopter")).isZero();
        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(thopter.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(thopter.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
        assertThat(thopter.isTapped()).isFalse();
        assertThat(thopter.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("ETB still creates a Thopter after Aspiring Aeronaut is destroyed in response")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        harness.setHand(player1, List.of(new AspiringAeronaut()));
        harness.setHand(player2, List.of(new FieryImpulse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent aeronaut = findPermanent(player1, "Aspiring Aeronaut");
        assertThat(countPermanents(player1, "Thopter")).isZero();

        harness.castAndResolveInstant(player2, 0, aeronaut.getId());
        harness.assertInGraveyard(player1, "Aspiring Aeronaut");
        assertThat(countPermanents(player1, "Aspiring Aeronaut")).isZero();
        assertThat(countPermanents(player1, "Thopter")).isZero();

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Thopter")).isEqualTo(1);
        assertThat(countPermanents(player2, "Thopter")).isZero();
    }
}
