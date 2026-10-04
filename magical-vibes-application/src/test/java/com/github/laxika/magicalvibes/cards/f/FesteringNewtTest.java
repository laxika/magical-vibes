package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BogbrewWitch;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RumblingBaloth;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FesteringNewt.class, BogbrewWitch.class, GrizzlyBears.class})
class FesteringNewtTest extends BaseCardTest {

    /**
     * Sets up combat where Festering Newt (player1) attacks and is blocked by a 3/3, so the Newt
     * dies from combat damage and its death trigger goes on the stack.
     */
    private void setupCombatWhereNewtDies() {
        Permanent newt = findPermanent(player1, "Festering Newt");
        newt.setSummoningSick(false);
        newt.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
        Permanent blocker = addCreatureReady(player2, bigBear);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    private Permanent permanentById(UUID id) {
        return harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(id))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Death trigger gives -1/-1 without a Bogbrew Witch")
    void deathTriggerGivesMinusOne() {
        harness.addToBattlefield(player1, new FesteringNewt());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereNewtDies();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        Permanent bears = permanentById(bearsId);
        assertThat(bears.getPowerModifier()).isEqualTo(-1);
        assertThat(bears.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Death trigger gives -4/-4 instead while controller has a Bogbrew Witch")
    void deathTriggerGivesMinusFourWithWitch() {
        harness.addToBattlefield(player1, new FesteringNewt());
        harness.addToBattlefield(player1, new BogbrewWitch());

        GrizzlyBears tough = new GrizzlyBears();
        tough.setPower(5);
        tough.setToughness(5);
        harness.addToBattlefield(player2, tough);
        UUID toughId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereNewtDies();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, toughId);
        harness.passBothPriorities();

        Permanent target = permanentById(toughId);
        assertThat(target.getPowerModifier()).isEqualTo(-4);
        assertThat(target.getToughnessModifier()).isEqualTo(-4);
    }

    @Test
    @DisplayName("A Bogbrew Witch controlled by the opponent does not upgrade the debuff")
    void opponentsWitchDoesNotUpgrade() {
        harness.addToBattlefield(player1, new FesteringNewt());
        harness.addToBattlefield(player2, new BogbrewWitch());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereNewtDies();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        Permanent bears = permanentById(bearsId);
        assertThat(bears.getPowerModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Only creatures an opponent controls are legal targets")
    void onlyOpponentCreaturesAreLegalTargets() {
        harness.addToBattlefield(player1, new FesteringNewt());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID ownBearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereNewtDies();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(opponentBearsId);
        assertThat(choice.validIds()).doesNotContain(ownBearsId);
    }

    @Test
    @DisplayName("Debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new FesteringNewt());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereNewtDies();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        assertThat(permanentById(bearsId).getPowerModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = permanentById(bearsId);
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @CardUsed({RumblingBaloth.class})
    @DisplayName("A Witch entering after the Newt dies upgrades the resolving trigger")
    void witchEnteringBeforeResolutionUpgradesDebuff() {
        Permanent newt = harness.addToBattlefieldAndReturn(player1, new FesteringNewt());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RumblingBaloth());
        newt.setToughnessModifier(-1);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        harness.addToBattlefield(player1, new BogbrewWitch());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rumbling Baloth");
        harness.assertNotOnBattlefield(player2, "Rumbling Baloth");
    }

    @Test
    @CardUsed({RumblingBaloth.class})
    @DisplayName("A Witch dying before resolution leaves only the normal debuff")
    void witchLeavingBeforeResolutionDoesNotUpgradeDebuff() {
        Permanent newt = harness.addToBattlefieldAndReturn(player1, new FesteringNewt());
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new BogbrewWitch());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RumblingBaloth());
        newt.setToughnessModifier(-1);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        witch.setToughnessModifier(-3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bogbrew Witch");
        harness.assertOnBattlefield(player2, "Rumbling Baloth");
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @CardUsed({RumblingBaloth.class, SongOfTheDryads.class})
    @DisplayName("A Bogbrew Witch that is a noncreature land does not upgrade the debuff")
    void noncreatureWitchDoesNotUpgradeDebuff() {
        Permanent newt = harness.addToBattlefieldAndReturn(player1, new FesteringNewt());
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new BogbrewWitch());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RumblingBaloth());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, witch.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, witch)).isFalse();
        assertThat(gqs.isLand(gd, witch)).isTrue();

        newt.setToughnessModifier(-1);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Rumbling Baloth");
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
    }
}
