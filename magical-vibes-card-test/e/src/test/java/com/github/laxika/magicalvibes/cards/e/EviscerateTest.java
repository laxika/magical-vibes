package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.cards.a.AdamantWill;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Eviscerate.class, PrimordialWurm.class, Forest.class, AdamantWill.class})
class EviscerateTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Eviscerate targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());

        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(Eviscerate.class);
        assertThat(entry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new PrimordialWurm());

        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Resolving Eviscerate destroys target creature and moves it to graveyard")
    void resolvingDestroysTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());

        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Primordial Wurm");
        harness.assertInGraveyard(player2, "Primordial Wurm");
        harness.assertInGraveyard(player1, "Eviscerate");
    }

    @Test
    @DisplayName("Eviscerate goes to graveyard after resolving")
    void evisceratGoesToGraveyardAfterResolving() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());

        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Eviscerate");
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());

        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, bears.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Eviscerate");
    }
    @Test
    @DisplayName("Can destroy a creature controlled by its caster")
    void destroysOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Primordial Wurm");
        harness.assertInGraveyard(player1, "Primordial Wurm");
        harness.assertInGraveyard(player1, "Eviscerate");
    }

    @Test
    @DisplayName("Indestructible gained in response prevents destruction")
    void indestructiblePreventsDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setHand(player2, List.of(new AdamantWill()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Primordial Wurm");
        harness.assertNotInGraveyard(player2, "Primordial Wurm");
        harness.assertInGraveyard(player1, "Eviscerate");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
