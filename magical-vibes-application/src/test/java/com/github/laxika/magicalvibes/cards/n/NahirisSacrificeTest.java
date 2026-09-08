package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NahirisSacrificeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices an artifact and divides damage equal to its mana value among creatures")
    void sacrificesArtifactAndDividesDamageEqualToManaValue() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new NahirisSacrifice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 0, null, Map.of(first.getId(), 1, second.getId(), 2),
                List.of(), List.of(), false, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(sacrifice.getId()));
        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Darksteel Ingot");
        harness.assertInGraveyard(player1, "Nahiri's Sacrifice");
    }

    @Test
    @DisplayName("Accepts a creature as the sacrificed permanent")
    void acceptsCreatureAsSacrifice() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new NahirisSacrifice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 0, null, Map.of(target.getId(), 2),
                List.of(), List.of(), false, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot sacrifice a non-artifact noncreature permanent")
    void cannotSacrificeNonArtifactNoncreaturePermanent() {
        Permanent sacrifice = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        harness.setHand(player1, List.of(new NahirisSacrifice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
