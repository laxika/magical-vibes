package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErebosBleakHearted.class, Forest.class, GrizzlyBears.class, HillGiant.class, Shock.class,
        WalkingCorpse.class})
class ErebosBleakHeartedTest extends BaseCardTest {

    @Test
    @DisplayName("Erebos is not a creature below five devotion to black")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent erebos = addErebos();
        addBlackPermanents(3);

        assertThat(gqs.isCreature(gd, erebos)).isFalse();
        assertThat(gqs.isEnchantment(gd, erebos)).isTrue();
    }

    @Test
    @DisplayName("Erebos becomes a creature at five devotion to black")
    void becomesCreatureAtDevotionThreshold() {
        Permanent erebos = addErebos();
        addBlackPermanents(4);

        assertThat(gqs.isCreature(gd, erebos)).isTrue();
    }

    @Test
    @DisplayName("Paying 2 life after another creature dies draws a card")
    void payingLifeAfterAllyCreatureDiesDrawsCard() {
        addErebos();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);

        killCreatureWithShock();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining the death trigger does not draw a card or cost life")
    void decliningDeathTriggerDoesNothing() {
        addErebos();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);

        killCreatureWithShock();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Sacrificing another creature gives a target creature -2/-1 until end of turn")
    void sacrificingAnotherCreatureDebuffsTargetCreature() {
        addErebos();
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability's debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        addErebos();
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The activated ability cannot sacrifice Erebos itself")
    void cannotSacrificeErebosItself() {
        addErebos();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addErebos() {
        return harness.addToBattlefieldAndReturn(player1, new ErebosBleakHearted());
    }

    private void addBlackPermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new WalkingCorpse());
        }
    }

    private void killCreatureWithShock() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID creatureId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player2, 0, creatureId);
        harness.passBothPriorities();
    }
}
