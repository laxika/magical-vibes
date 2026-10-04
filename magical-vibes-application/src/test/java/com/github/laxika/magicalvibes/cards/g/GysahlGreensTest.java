package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GysahlGreens.class, Forest.class})
class GysahlGreensTest extends BaseCardTest {

    @Test
    void createsABirdToken() {
        castFromHand();

        Permanent bird = findPermanent(player1, "Bird");
        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(2);
    }

    @Test
    void birdGetsStrongerWhenALandEnters() {
        harness.setHand(player1, List.of(new GysahlGreens(), new Forest()));
        addCastingMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        Permanent bird = findPermanent(player1, "Bird");

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(2);
    }

    @Test
    void flashbackCreatesABirdAndExilesGysahlGreens() {
        harness.setGraveyard(player1, List.of(new GysahlGreens()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Bird")).isNotNull();
        harness.assertNotInGraveyard(player1, "Gysahl Greens");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Gysahl Greens"));
    }

    @Test
    void landfallBoostExpiresAtEndOfTurn() {
        castFromHand();
        Permanent bird = findPermanent(player1, "Bird");
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(3);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(2);
    }

    @Test
    void eachLandEntryAddsAnotherBoost() {
        castFromHand();
        Permanent bird = findPermanent(player1, "Bird");

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(2);
    }

    @Test
    void opponentsLandDoesNotBoostBird() {
        castFromHand();
        Permanent bird = findPermanent(player1, "Bird");

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(2);
    }

    @Test
    void flashbackBirdHasLandfallAbility() {
        harness.setGraveyard(player1, List.of(new GysahlGreens()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        Permanent bird = findPermanent(player1, "Bird");

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(2);
    }
    private void castFromHand() {
        harness.castFromHand(player1, new GysahlGreens(), "{1}{G}");
        harness.passBothPriorities();
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
