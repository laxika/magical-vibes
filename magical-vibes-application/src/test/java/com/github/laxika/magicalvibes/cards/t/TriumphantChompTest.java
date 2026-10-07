package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.e.EarthshakerDreadmaw;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TriumphantChomp.class, AirElemental.class, ColossalDreadmaw.class, FountainOfYouth.class,
        HillGiant.class, EarthshakerDreadmaw.class})
class TriumphantChompTest extends BaseCardTest {

    @Test
    @DisplayName("Deals at least 2 damage and ignores non-Dinosaurs")
    void dealsMinimumDamageAndIgnoresNonDinosaurs() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new TriumphantChomp()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals damage equal to the greatest Dinosaur power you control")
    void usesGreatestDinosaurPower() {
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new TriumphantChomp()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Air Elemental"));

        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Ignores Dinosaurs controlled by an opponent")
    void ignoresOpponentsDinosaurs() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new TriumphantChomp()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new TriumphantChomp()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Uses the greatest current Dinosaur power rather than adding their powers")
    void usesMaximumRatherThanTotalPower() {
        Permanent smaller = harness.addToBattlefieldAndReturn(player1, new EarthshakerDreadmaw());
        smaller.setPowerModifier(-3);
        Permanent larger = harness.addToBattlefieldAndReturn(player1, new EarthshakerDreadmaw());
        larger.setPowerModifier(-1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EarthshakerDreadmaw());
        harness.setHand(player1, List.of(new TriumphantChomp()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertOnBattlefield(player2, "Earthshaker Dreadmaw");
    }

    @Test
    @DisplayName("Deals 2 damage when the only controlled Dinosaur has power below 2")
    void keepsMinimumWhenDinosaurPowerIsLow() {
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new EarthshakerDreadmaw());
        dinosaur.setPowerModifier(-5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EarthshakerDreadmaw());
        harness.setHand(player1, List.of(new TriumphantChomp()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Recalculates Dinosaur power when the spell resolves")
    void usesPowerAtResolution() {
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new EarthshakerDreadmaw());
        dinosaur.setPowerModifier(-3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EarthshakerDreadmaw());
        harness.setHand(player1, List.of(new TriumphantChomp()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, target.getId());
        dinosaur.setPowerModifier(-1);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertOnBattlefield(player2, "Earthshaker Dreadmaw");
    }

    @Test
    @DisplayName("Falls back to 2 damage if the only controlled Dinosaur leaves before resolution")
    void ignoresDinosaurThatLeftBeforeResolution() {
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new EarthshakerDreadmaw());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EarthshakerDreadmaw());
        harness.setHand(player1, List.of(new TriumphantChomp()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dinosaur));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Earthshaker Dreadmaw");
    }
}
