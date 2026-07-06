package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostFirstTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.FirstTargetFightsSecondTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChelonianTackleTest extends BaseCardTest {

    

    @Test
    @DisplayName("Boost only — single target creature you control gets +0/+10")
    void singleTargetBoostOnly() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChelonianTackle()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(bearId));
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isEqualTo(10);
    }

    @Test
    @DisplayName("Boost applies before fight — small creature survives opponent's power damage")
    void boostAppliesBeforeFight() {
        // Llanowar Elves (1/1) gets +0/+10 → 1/11, then fights Grizzly Bears (2/2)
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChelonianTackle()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(elvesId, bearId));
        harness.passBothPriorities();

        Permanent elves = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(elves.getToughnessModifier()).isEqualTo(10);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Fight kills smaller opponent creature while boosted creature survives")
    void fightKillsOpponentCreature() {
        // Grizzly Bears (2/2) gets +0/+10 → 2/12, then fights Llanowar Elves (1/1)
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new ChelonianTackle()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castSorcery(player1, 0, List.of(bearId, elvesId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot target opponent creature as first target")
    void cannotTargetOpponentCreatureAsFirstTarget() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new ChelonianTackle()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearId, elvesId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Cannot target own creature as second target")
    void cannotTargetOwnCreatureAsSecondTarget() {
        GrizzlyBears bear1 = new GrizzlyBears();
        GrizzlyBears bear2 = new GrizzlyBears();
        harness.addToBattlefield(player1, bear1);
        harness.addToBattlefield(player1, bear2);
        harness.setHand(player1, List.of(new ChelonianTackle()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player1.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(id1, id2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Boost still applies when second target removed before resolution")
    void boostAppliesWhenSecondTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new ChelonianTackle()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castSorcery(player1, 0, List.of(bearId, elvesId));

        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getToughnessModifier()).isEqualTo(10);
    }
}
