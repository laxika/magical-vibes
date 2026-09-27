package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GnatMiser;
import com.github.laxika.magicalvibes.cards.o.OboroPalaceInTheClouds;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunderFromWithin.class, PithingNeedle.class,
        OboroPalaceInTheClouds.class, GnatMiser.class})
class SunderFromWithinTest extends BaseCardTest {

    @Test
    @DisplayName("Sunder from Within destroys target artifact")
    void destroysArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());
        harness.setHand(player1, List.of(new SunderFromWithin()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Pithing Needle");
        harness.assertInGraveyard(player2, "Pithing Needle");
    }

    @Test
    @DisplayName("Sunder from Within destroys target land")
    void destroysLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OboroPalaceInTheClouds());
        harness.setHand(player1, List.of(new SunderFromWithin()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Oboro, Palace in the Clouds");
        harness.assertInGraveyard(player2, "Oboro, Palace in the Clouds");
    }

    @Test
    @DisplayName("Sunder from Within can destroy its controller's artifact")
    void destroysOwnArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PithingNeedle());
        harness.setHand(player1, List.of(new SunderFromWithin()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pithing Needle");
        harness.assertInGraveyard(player1, "Pithing Needle");
    }

    @Test
    @DisplayName("Sunder from Within cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GnatMiser());
        harness.setHand(player1, List.of(new SunderFromWithin()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
