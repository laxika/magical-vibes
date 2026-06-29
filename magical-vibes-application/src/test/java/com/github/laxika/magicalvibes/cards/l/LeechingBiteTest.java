package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LeechingBiteTest extends BaseCardTest {

    @Test
    @DisplayName("First target gets +1/+1 and second target gets -1/-1")
    void boostsFirstAndDebuffsSecond() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LeechingBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID firstId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID secondId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(firstId, secondId));
        harness.passBothPriorities();

        Permanent first = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(first.getToughnessModifier()).isEqualTo(1);

        Permanent second = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(second.getPowerModifier()).isEqualTo(-1);
        assertThat(second.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("-1/-1 kills a 1/1 creature")
    void debuffKillsOneOneCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new LeechingBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID firstId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID secondId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(firstId, secondId));
        harness.passBothPriorities();

        Permanent first = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(first.getToughnessModifier()).isEqualTo(1);

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Can target two creatures you control")
    void canTargetOwnCreatures() {
        GrizzlyBears bear1 = new GrizzlyBears();
        GrizzlyBears bear2 = new GrizzlyBears();
        harness.addToBattlefield(player1, bear1);
        harness.addToBattlefield(player1, bear2);
        harness.setHand(player1, List.of(new LeechingBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();
        harness.castInstant(player1, 0, List.of(id1, id2));
        harness.passBothPriorities();

        Permanent first = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(first.getToughnessModifier()).isEqualTo(1);

        Permanent second = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(second.getPowerModifier()).isEqualTo(-1);
        assertThat(second.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Spell fizzles when all targets removed before resolution")
    void fizzlesWhenAllTargetsRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LeechingBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID firstId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID secondId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(firstId, secondId));

        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Boost still applies when second target removed before resolution")
    void boostAppliesWhenSecondTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LeechingBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID firstId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID secondId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(firstId, secondId));

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        Permanent first = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(first.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debuff still applies when first target removed before resolution")
    void debuffAppliesWhenFirstTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LeechingBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID firstId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID secondId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(firstId, secondId));

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        Permanent second = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(second.getPowerModifier()).isEqualTo(-1);
        assertThat(second.getToughnessModifier()).isEqualTo(-1);
    }
}
