package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Asphyxiate;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NyxbornRollicker;
import com.github.laxika.magicalvibes.cards.n.NyxbornTriton;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SearingBlood.class, FugitiveWizard.class, GrizzlyBears.class, CruelEdict.class,
        NyxbornRollicker.class, NyxbornTriton.class, Asphyxiate.class})
class SearingBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage, then deals 3 damage to the creature's controller when it dies")
    void dealsThreeDamageWhenTheTargetDies() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        castSearingBlood(harness.getPermanentId(player2, "Fugitive Wizard"));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Fugitive Wizard");
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Triggers if the damaged creature dies later in the same turn")
    void triggersWhenTargetDiesLaterInTheTurn() {
        GrizzlyBears card = new GrizzlyBears();
        card.setToughness(3);
        harness.addToBattlefield(player2, card);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castSearingBlood(targetId);
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not register the delayed trigger when the target is gone at resolution")
    void doesNotTriggerWhenTargetIsGoneAtResolution() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new SearingBlood()));
        harness.addMana(player1, ManaColor.RED, 2);
        UUID targetId = harness.getPermanentId(player2, "Fugitive Wizard");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Damage to the controller waits for the delayed trigger to resolve")
    void controllerDamageUsesTheStack() {
        harness.addToBattlefield(player2, new NyxbornRollicker());
        castSearingBlood(harness.getPermanentId(player2, "Nyxborn Rollicker"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Nyxborn Rollicker");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Can target your own creature and damage you when it dies")
    void damagesControllerOfOwnCreature() {
        harness.addToBattlefield(player1, new NyxbornRollicker());
        castSearingBlood(harness.getPermanentId(player1, "Nyxborn Rollicker"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Nyxborn Rollicker");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Two resolved copies each trigger when the same creature dies")
    void multipleSpellsEachRegisterADeathTrigger() {
        harness.addToBattlefield(player2, new NyxbornTriton());
        UUID targetId = harness.getPermanentId(player2, "Nyxborn Triton");
        castSearingBlood(targetId);
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Nyxborn Triton");
        harness.assertLife(player2, 20);

        castSearingBlood(targetId);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Nyxborn Triton");
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("A surviving real creature dying to another spell still triggers")
    void triggersWhenSurvivorIsDestroyedLater() {
        harness.addToBattlefield(player2, new NyxbornTriton());
        UUID targetId = harness.getPermanentId(player2, "Nyxborn Triton");
        castSearingBlood(targetId);
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Nyxborn Triton");
        harness.assertLife(player2, 20);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Asphyxiate()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, targetId);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Nyxborn Triton");
        harness.assertLife(player2, 17);
    }

    private void castSearingBlood(UUID targetId) {
        harness.setHand(player1, List.of(new SearingBlood()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, targetId);
    }
}
