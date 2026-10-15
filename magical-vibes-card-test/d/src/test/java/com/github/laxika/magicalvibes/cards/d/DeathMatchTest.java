package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BarrenMoor;
import com.github.laxika.magicalvibes.cards.b.BarkhideMauler;
import com.github.laxika.magicalvibes.cards.c.CrudeRampart;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathMatch.class, BarkhideMauler.class, CrudeRampart.class, BarrenMoor.class,
        Opalescence.class})
class DeathMatchTest extends BaseCardTest {

    @Test
    void enteringCreatureControllerChoosesCreatureAndDebuffExpires() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CrudeRampart());
        harness.addToBattlefield(player1, new DeathMatch());

        harness.castFromHand(player1, new BarkhideMauler(), "{4}{G}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validPermanentIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    void enteringOpponentsCreatureControllerChoosesTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CrudeRampart());
        harness.addToBattlefield(player1, new DeathMatch());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new BarkhideMauler(), "{4}{G}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validPermanentIds()).contains(target.getId());
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void decliningDoesNotDebuffTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CrudeRampart());
        harness.addToBattlefield(player1, new DeathMatch());

        harness.castFromHand(player1, new BarkhideMauler(), "{4}{G}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    void enteringCreatureCanBeChosenAsTarget() {
        harness.addToBattlefield(player1, new DeathMatch());
        harness.castFromHand(player1, new BarkhideMauler(), "{4}{G}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent entering = findPermanent(player1, "Barkhide Mauler");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(entering.getId());
        harness.handlePermanentChosen(player1, entering.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, entering)).isEqualTo(1);
    }

    @Test
    void noncreatureEnteringDoesNotTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CrudeRampart());
        harness.addToBattlefield(player1, new DeathMatch());
        harness.setHand(player1, List.of(new BarrenMoor()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @CardUsed({DeathMatch.class, CrudeRampart.class, Opalescence.class})
    void triggersOnItsOwnEntryWhenOpalescenceMakesItACreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CrudeRampart());
        harness.addToBattlefield(player1, new Opalescence());
        harness.castFromHand(player1, new DeathMatch(), "{3}{B}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validPermanentIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @CardUsed({DeathMatch.class, CrudeRampart.class, Opalescence.class})
    void anotherEnchantmentEnteringAsACreatureTriggers() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CrudeRampart());
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new DeathMatch());
        harness.castFromHand(player1, new Opalescence(), "{2}{W}{W}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validPermanentIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void faceDownCreatureEntryTriggersAndMinusThreeToughnessPutsItInGraveyard() {
        harness.addToBattlefield(player1, new DeathMatch());
        harness.setHand(player1, List.of(new CrudeRampart()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent entering = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .findFirst().orElseThrow();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(entering.getId());
        harness.handlePermanentChosen(player1, entering.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(entering);
        harness.assertInGraveyard(player1, "Crude Rampart");
    }
}
