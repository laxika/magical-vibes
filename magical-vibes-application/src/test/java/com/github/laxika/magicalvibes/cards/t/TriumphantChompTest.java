package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
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
        HillGiant.class})
class TriumphantChompTest extends BaseCardTest {

    @Test
    @DisplayName("Deals at least 2 damage and ignores non-Dinosaurs")
    void dealsMinimumDamageAndIgnoresNonDinosaurs() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new TriumphantChomp()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals damage equal to the greatest Dinosaur power you control")
    void usesGreatestDinosaurPower() {
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new TriumphantChomp()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Air Elemental"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Ignores Dinosaurs controlled by an opponent")
    void ignoresOpponentsDinosaurs() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new TriumphantChomp()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

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
}
