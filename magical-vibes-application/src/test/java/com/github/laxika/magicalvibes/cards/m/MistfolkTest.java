package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArcTrail;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MistfolkTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell targeting Mistfolk")
    void countersSpellTargetingSelf() {
        Mistfolk mistfolk = new Mistfolk();
        harness.addToBattlefield(player1, mistfolk);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Mistfolk"));
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Mistfolk");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters a multi-target spell that targets Mistfolk")
    void countersMultiTargetSpellTargetingSelf() {
        Mistfolk mistfolk = new Mistfolk();
        harness.addToBattlefield(player1, mistfolk);

        ArcTrail arcTrail = new ArcTrail();
        harness.setHand(player2, List.of(arcTrail));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0,
                List.of(harness.getPermanentId(player1, "Mistfolk"), player2.getId()));
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, arcTrail.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Arc Trail");
        harness.assertOnBattlefield(player1, "Mistfolk");
        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a spell targeting another creature")
    void cannotTargetSpellTargetingOtherCreature() {
        Mistfolk mistfolk = new Mistfolk();
        harness.addToBattlefield(player1, mistfolk);
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-targeting spell")
    void cannotTargetNonTargetingSpell() {
        Mistfolk mistfolk = new Mistfolk();
        harness.addToBattlefield(player1, mistfolk);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without {U}")
    void cannotActivateWithoutBlueMana() {
        Mistfolk mistfolk = new Mistfolk();
        harness.addToBattlefield(player1, mistfolk);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Mistfolk"));
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
