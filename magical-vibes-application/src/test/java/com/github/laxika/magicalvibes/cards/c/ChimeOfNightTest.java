package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ApprenticeNecromancer;
import com.github.laxika.magicalvibes.cards.d.Donate;
import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChimeOfNight.class, ApprenticeNecromancer.class, MetathranSoldier.class, Donate.class})
class ChimeOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("When put into a graveyard from the battlefield, it destroys a target nonblack creature")
    void destroysTargetNonblackCreature() {
        Permanent enchantedBlackCreature = addCreatureReady(player2, new ApprenticeNecromancer());
        Permanent targetCreature = addCreatureReady(player2, new MetathranSoldier());
        harness.setHand(player1, List.of(new ChimeOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, enchantedBlackCreature.getId());
        harness.passBothPriorities();

        Permanent chime = findPermanent(player1, "Chime of Night");
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, chime));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, targetCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Metathran Soldier");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(enchantedBlackCreature.getId()));
    }

    @Test
    @DisplayName("Does not target a black creature")
    void doesNotTargetBlackCreature() {
        Permanent blackCreature = addCreatureReady(player2, new ApprenticeNecromancer());
        Permanent enchantedCreature = addCreatureReady(player2, new MetathranSoldier());
        harness.setHand(player1, List.of(new ChimeOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, enchantedCreature.getId());
        harness.passBothPriorities();

        Permanent chime = findPermanent(player1, "Chime of Night");
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, chime));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .contains(enchantedCreature.getId())
                .doesNotContain(blackCreature.getId());

        harness.handlePermanentChosen(player1, enchantedCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Metathran Soldier");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(blackCreature.getId()));
    }

    @Test
    @DisplayName("The controller immediately before it leaves controls the trigger")
    void formerControllerControlsTriggerAfterAuraIsStolen() {
        Permanent enchantedBlackCreature = addCreatureReady(player2, new ApprenticeNecromancer());
        Permanent targetCreature = addCreatureReady(player2, new MetathranSoldier());
        harness.setHand(player1, List.of(new ChimeOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, enchantedBlackCreature.getId());
        harness.passBothPriorities();

        Permanent chime = findPermanent(player1, "Chime of Night");
        harness.setHand(player1, List.of(new Donate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), chime.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(chime);

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, chime));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds())
                .contains(targetCreature.getId())
                .doesNotContain(enchantedBlackCreature.getId());

        harness.handlePermanentChosen(player2, targetCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Metathran Soldier");
    }
}
