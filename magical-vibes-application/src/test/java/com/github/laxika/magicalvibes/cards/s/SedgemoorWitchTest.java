package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HuntForSpecimens;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SedgemoorWitch.class, BarkshellBlessing.class, GrizzlyBears.class,
        ProdigalPyromancer.class, Shock.class, HuntForSpecimens.class})
class SedgemoorWitchTest extends BaseCardTest {

    @Test
    @DisplayName("Casting and copying an instant creates a Pest for each magecraft trigger")
    void castingAndCopyingInstantCreatesPests() {
        addCreatureReady(player1, new SedgemoorWitch());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(),
                List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Pest")).isEqualTo(2);
    }

    @Test
    @DisplayName("A Pest created by Sedgemoor Witch gains its controller 1 life when it dies")
    void pestDeathGainsLife() {
        addCreatureReady(player1, new SedgemoorWitch());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent pest = findPermanent(player1, "Pest");
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, pest.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(pest.getId()));
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Ward counters an opponent's activated ability unless they pay 3 life")
    void wardCountersOpponentAbility() {
        Permanent witch = addCreatureReady(player1, new SedgemoorWitch());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, witch.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(witch.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingWardAllowsOpponentSpellToResolve() {
        Permanent witch = addCreatureReady(player1, new SedgemoorWitch());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, witch.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertNotOnBattlefield(player1, "Sedgemoor Witch");
        assertThat(countPermanents(player1, "Pest")).isZero();
    }

    @Test
    void decliningWardCountersOpponentSpell() {
        addCreatureReady(player1, new SedgemoorWitch());
        Permanent witch = findPermanent(player1, "Sedgemoor Witch");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, witch.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Sedgemoor Witch");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(countPermanents(player1, "Pest")).isZero();
    }

    @Test
    void ownSpellDoesNotTriggerWardAndCreatesPestBeforeWitchDies() {
        Permanent witch = addCreatureReady(player1, new SedgemoorWitch());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, witch.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Sedgemoor Witch");
        assertThat(countPermanents(player1, "Pest")).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void castingSorceryCreatesAdditionalPest() {
        addCreatureReady(player1, new SedgemoorWitch());
        harness.setHand(player1, List.of(new HuntForSpecimens()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Pest")).isEqualTo(2);
    }

    @Test
    void castingCreatureDoesNotTriggerMagecraft() {
        addCreatureReady(player1, new SedgemoorWitch());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Pest")).isZero();
    }
}
