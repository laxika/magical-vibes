package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cathodion;
import com.github.laxika.magicalvibes.cards.c.ClawsOfGix;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.p.PowerSink;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkitteringSkirge.class, GorillaWarrior.class, ClawsOfGix.class, Cathodion.class, PowerSink.class})
class SkitteringSkirgeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself when its controller casts a creature spell")
    void sacrificesItselfWhenControllerCastsCreatureSpell() {
        harness.addToBattlefieldAndReturn(player1, new SkitteringSkirge());
        harness.setHand(player1, List.of(new GorillaWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Skittering Skirge");
    }

    @Test
    @DisplayName("Does not sacrifice itself for a noncreature spell")
    void doesNotSacrificeItselfForNoncreatureSpell() {
        Permanent skitteringSkirge = harness.addToBattlefieldAndReturn(player1, new SkitteringSkirge());
        harness.setHand(player1, List.of(new ClawsOfGix()));

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
        harness.setHand(player2, List.of(new GorillaWarrior()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castCreature(player2, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skitteringSkirge);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Sacrifices itself when an artifact creature spell is cast")
    void sacrificesItselfForArtifactCreatureSpell() {
        harness.addToBattlefieldAndReturn(player1, new SkitteringSkirge());
        harness.setHand(player1, List.of(new Cathodion()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Skittering Skirge");
    }

    @Test
    @DisplayName("Sacrifices itself even when the creature spell is countered")
    void sacrificesItselfWhenCreatureSpellIsCountered() {
        harness.addToBattlefieldAndReturn(player1, new SkitteringSkirge());
        Cathodion cathodion = new Cathodion();
        harness.setHand(player1, List.of(cathodion));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, cathodion.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Skittering Skirge");
        harness.assertInGraveyard(player1, "Cathodion");
    }
}
