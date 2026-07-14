package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JokulhaupsTest extends BaseCardTest {

    @Test
    @DisplayName("Jokulhaups destroys all artifacts, creatures, and lands")
    void destroysArtifactsCreaturesAndLands() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new JinxedIdol());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new Jokulhaups()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Jinxed Idol");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Destroyed permanents can't be regenerated")
    void destroyedPermanentsCannotBeRegenerated() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Jokulhaups()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Regeneration shield does not save the creature from Jokulhaups.
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
