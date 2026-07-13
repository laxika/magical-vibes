package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReknitTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Reknit grants a regeneration shield to target creature")
    void grantsShieldToCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Reknit()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Reknit can target a noncreature permanent")
    void canTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Reknit()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");
        harness.castInstant(player1, 0, fountainId);
        harness.passBothPriorities();

        Permanent fountain = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(fountain.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Reknit can be paid with white mana for the hybrid symbol")
    void payableWithWhiteMana() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Reknit()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getRegenerationShield()).isEqualTo(1);
    }
}
