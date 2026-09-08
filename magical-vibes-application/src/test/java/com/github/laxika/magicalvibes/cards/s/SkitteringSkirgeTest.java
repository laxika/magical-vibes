package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkitteringSkirgeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself when its controller casts a creature spell")
    void sacrificesItselfWhenControllerCastsCreatureSpell() {
        harness.addToBattlefieldAndReturn(player1, new SkitteringSkirge());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Skittering Skirge");
    }

    @Test
    @DisplayName("Does not sacrifice itself for a noncreature spell")
    void doesNotSacrificeItselfForNoncreatureSpell() {
        Permanent skitteringSkirge = harness.addToBattlefieldAndReturn(player1, new SkitteringSkirge());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skitteringSkirge);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not sacrifice itself when an opponent casts a creature spell")
    void doesNotSacrificeItselfForOpponentsCreatureSpell() {
        Permanent skitteringSkirge = harness.addToBattlefieldAndReturn(player1, new SkitteringSkirge());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skitteringSkirge);
        assertThat(gd.stack).hasSize(1);
    }
}
