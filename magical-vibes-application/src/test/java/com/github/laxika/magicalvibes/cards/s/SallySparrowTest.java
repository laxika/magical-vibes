package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SallySparrow.class, GrizzlyBears.class, Shock.class, Unsummon.class, WrathOfGod.class})
class SallySparrowTest extends BaseCardTest {

    @Test
    void controllerCanCastCreatureSpellsAtInstantSpeed() {
        harness.addToBattlefield(player1, new SallySparrow());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    void onlyControllerGetsCreatureSpellFlash() {
        harness.addToBattlefield(player1, new SallySparrow());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void investigatesWhenAnotherControlledCreatureLeavesAndOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new SallySparrow());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);

        UUID firstBearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, firstBearId);
        harness.passBothPriorities();

        UUID secondBearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, secondBearId);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Clue")).isOne();
    }

    @Test
    void investigatesWhenAnotherCreatureReturnsToHandAndClueCanDraw() {
        harness.addToBattlefield(player1, new SallySparrow());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Clue")).isOne();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int clueIndex = gd.playerBattlefields.get(player1.getId()).size() - 1;
        harness.activateAbility(player1, clueIndex, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Clue")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void doesNotInvestigateWhenOpponentCreatureLeaves() {
        harness.addToBattlefield(player1, new SallySparrow());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Clue")).isZero();
    }

    @Test
    void doesNotInvestigateWhenSallyAloneLeaves() {
        harness.addToBattlefield(player1, new SallySparrow());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Sally Sparrow"));

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Clue")).isZero();
    }

    @Test
    void investigatesWhenSallyAndOtherCreaturesDieSimultaneously() {
        harness.addToBattlefield(player1, new SallySparrow());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sally Sparrow");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Clue")).isOne();
    }

    @Test
    void doesNotGrantFlashToNoncreatureSpells() {
        harness.addToBattlefield(player1, new SallySparrow());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
