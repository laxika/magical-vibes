package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BringToTrial.class, AirElemental.class, HillGiant.class})
class BringToTrialTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target creature with power 4 or greater")
    void exilesHighPowerCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        UUID targetId = target.getId();

        harness.setHand(player1, List.of(new BringToTrial()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertNotInGraveyard(player2, "Air Elemental");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetLowPowerCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");

        harness.setHand(player1, List.of(new BringToTrial()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Can exile your own creature whose modified power reaches 4")
    void exilesOwnCreatureWithIncreasedPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        target.setPowerModifier(1);
        harness.setHand(player1, List.of(new BringToTrial()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertNotInGraveyard(player1, "Hill Giant");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
        harness.assertInGraveyard(player1, "Bring to Trial");
    }

    @Test
    @DisplayName("Does not exile a creature whose power drops below 4 before resolution")
    void targetBecomesIllegalWhenPowerDrops() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new BringToTrial()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, target.getId());
        target.setPowerModifier(-1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
        harness.assertInGraveyard(player1, "Bring to Trial");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature whose modified power is below 4")
    void cannotTargetCreatureWithReducedPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        target.setPowerModifier(-1);
        harness.setHand(player1, List.of(new BringToTrial()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }
}
