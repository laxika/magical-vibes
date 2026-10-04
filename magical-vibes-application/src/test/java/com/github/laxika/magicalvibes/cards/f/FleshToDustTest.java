package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.SliverHivelord;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FleshToDust.class, RuneclawBear.class, Forest.class, SliverHivelord.class})
class FleshToDustTest extends BaseCardTest {

    @Test
    @DisplayName("Flesh to Dust destroys target creature and ignores regeneration")
    void destroysTargetCreatureAndCannotBeRegenerated() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bears.setRegenerationShield(1);

        harness.setHand(player1, List.of(new FleshToDust()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Flesh to Dust cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new FleshToDust()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flesh to Dust can destroy its controller's creature")
    void destroysOwnCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new FleshToDust()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Flesh to Dust");
    }

    @Test
    @DisplayName("Preventing regeneration does not bypass indestructible")
    void cannotDestroyIndestructibleCreature() {
        Permanent hivelord = harness.addToBattlefieldAndReturn(player2, new SliverHivelord());
        harness.setHand(player1, List.of(new FleshToDust()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player1, 0, hivelord.getId());

        harness.assertOnBattlefield(player2, "Sliver Hivelord");
        harness.assertNotInGraveyard(player2, "Sliver Hivelord");
        harness.assertInGraveyard(player1, "Flesh to Dust");
    }
}
