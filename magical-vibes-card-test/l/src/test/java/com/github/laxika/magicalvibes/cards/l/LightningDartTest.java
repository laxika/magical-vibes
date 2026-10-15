package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.ArmoredGuardian;
import com.github.laxika.magicalvibes.cards.b.BenalishLancer;
import com.github.laxika.magicalvibes.cards.b.BlindSeer;
import com.github.laxika.magicalvibes.cards.k.KavuAggressor;
import com.github.laxika.magicalvibes.cards.m.MetathranAerostat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightningDart.class, ArmoredGuardian.class, BenalishLancer.class, KavuAggressor.class,
        MetathranAerostat.class, BlindSeer.class})
class LightningDartTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a red creature")
    void dealsOneDamageToOtherColor() {
        harness.addToBattlefield(player2, new KavuAggressor());
        harness.setHand(player1, List.of(new LightningDart()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Kavu Aggressor"));

        assertThat(findPermanent(player2, "Kavu Aggressor").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 4 damage to a blue creature instead")
    void dealsFourDamageToBlueCreature() {
        harness.addToBattlefield(player2, new MetathranAerostat());
        harness.setHand(player1, List.of(new LightningDart()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Metathran Aerostat"));

        harness.assertInGraveyard(player2, "Metathran Aerostat");
    }

    @Test
    @DisplayName("Deals exactly 4 damage to a multicolored white-blue creature")
    void dealsExactlyFourDamageToMulticoloredWhiteBlueCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredGuardian());
        harness.setHand(player1, List.of(new LightningDart()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Armored Guardian");
    }

    @Test
    @DisplayName("Deals 4 damage to a white creature instead")
    void dealsFourDamageToWhiteCreature() {
        harness.addToBattlefield(player2, new BenalishLancer());
        harness.setHand(player1, List.of(new LightningDart()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Benalish Lancer"));

        harness.assertInGraveyard(player2, "Benalish Lancer");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new LightningDart()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals 4 damage when a red target becomes blue in response")
    void targetBecomesBlueBeforeResolution() {
        harness.addToBattlefield(player1, new BlindSeer());
        harness.addToBattlefield(player2, new KavuAggressor());
        harness.setHand(player1, List.of(new LightningDart()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        var targetId = harness.getPermanentId(player2, "Kavu Aggressor");
        harness.castInstant(player1, 0, targetId);
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Kavu Aggressor");
    }

    @Test
    @DisplayName("Deals only 1 damage when a white-blue target becomes red in response")
    void targetLosesWhiteAndBlueBeforeResolution() {
        harness.addToBattlefield(player1, new BlindSeer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredGuardian());
        harness.setHand(player1, List.of(new LightningDart()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Armored Guardian");
    }

    @Test
    @DisplayName("Deals no damage when its target gains shroud in response")
    void targetGainsShroudBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArmoredGuardian());
        harness.setHand(player1, List.of(new LightningDart()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Armored Guardian");
        harness.assertInGraveyard(player1, "Lightning Dart");
    }
}
