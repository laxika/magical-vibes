package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.f.FountainportBell;
import com.github.laxika.magicalvibes.cards.i.IntoTheFloodMaw;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RottenmouthViper.class, Spellbook.class, Swamp.class, FountainportBell.class, IntoTheFloodMaw.class})
class RottenmouthViperTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a nonland permanent reduces the cost and the ETB trigger uses one blight counter")
    void sacrificesNonlandPermanentAndResolvesEtbTrigger() {
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new RottenmouthViper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithSacrificeForReduction(player1, 0, null, List.of(spellbook.getId()));
        resolveAllTriggers();

        Permanent viper = findPermanent(player1, "Rottenmouth Viper");
        assertThat(viper.getCounterCount(CounterType.BLIGHT)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("The attack trigger counts all blight counters on Rottenmouth Viper")
    void attackTriggerCountsExistingBlightCounters() {
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RottenmouthViper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithSacrificeForReduction(player1, 0, null, List.of());
        resolveAllTriggers();

        Permanent viper = findPermanent(player1, "Rottenmouth Viper");
        viper.setCounterCount(CounterType.BLIGHT, 1);
        viper.setSummoningSick(false);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(viper)));
        harness.passBothPriorities();

        assertThat(viper.getCounterCount(CounterType.BLIGHT)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent can discard instead of losing life to the enters trigger")
    void opponentDiscardsInsteadOfLosingLife() {
        harness.setHand(player2, List.of(new FountainportBell()));
        harness.setHand(player1, List.of(new RottenmouthViper()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreatureWithSacrificeForReduction(player1, 0, null, List.of());
        resolveAllTriggers();
        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Fountainport Bell");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent can sacrifice an artifact instead of losing life to the enters trigger")
    void opponentSacrificesNoncreaturePermanent() {
        Permanent bell = harness.addToBattlefieldAndReturn(player2, new FountainportBell());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new RottenmouthViper()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreatureWithSacrificeForReduction(player1, 0, null, List.of());
        resolveAllTriggers();
        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.SACRIFICE);
        harness.handlePermanentChosen(player2, bell.getId());

        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player2, "Fountainport Bell");
        harness.assertInGraveyard(player2, "Fountainport Bell");
    }

    @Test
    @DisplayName("More than five artifacts can be sacrificed while the black mana is still paid")
    void multipleSacrificesReduceAllGenericMana() {
        List<Permanent> bells = java.util.stream.IntStream.range(0, 6)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new FountainportBell()))
                .toList();
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new RottenmouthViper()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithSacrificeForReduction(player1, 0, null,
                bells.stream().map(Permanent::getId).toList());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Rottenmouth Viper");
        harness.assertNotOnBattlefield(player1, "Fountainport Bell");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Lands cannot be sacrificed to reduce the casting cost")
    void cannotSacrificeLandForReduction() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.setHand(player1, List.of(new RottenmouthViper()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castCreatureWithSacrificeForReduction(
                player1, 0, null, List.of(swamp.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Swamp");
        harness.assertInHand(player1, "Rottenmouth Viper");
    }

    @Test
    @DisplayName("Sacrifice choices are collected before any chosen permanents leave the battlefield")
    void sacrificesAreDeferredUntilAllChoicesAreMade() {
        Permanent viper = harness.addToBattlefieldAndReturn(player1, new RottenmouthViper());
        viper.setCounterCount(CounterType.BLIGHT, 1);
        viper.setSummoningSick(false);
        Permanent bell = harness.addToBattlefieldAndReturn(player2, new FountainportBell());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new FountainportBell()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(viper)));
            resolveAllTriggers();
            harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.SACRIFICE);
            harness.handlePermanentChosen(player2, bell.getId());

            harness.assertOnBattlefield(player2, "Fountainport Bell");
            harness.assertNotInGraveyard(player2, "Fountainport Bell");
            harness.assertLife(player2, 20);

            harness.handleListChoice(player2, "Lose 4 life");
            harness.assertLife(player2, 16);
            harness.assertInGraveyard(player2, "Fountainport Bell");
            harness.assertNotOnBattlefield(player2, "Fountainport Bell");
        });
    }

    @Test
    @DisplayName("Discard choices are collected before any chosen cards are discarded")
    void discardsAreDeferredUntilAllChoicesAreMade() {
        Permanent viper = harness.addToBattlefieldAndReturn(player1, new RottenmouthViper());
        viper.setCounterCount(CounterType.BLIGHT, 1);
        viper.setSummoningSick(false);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new FountainportBell(), new RottenmouthViper()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(viper)));
            resolveAllTriggers();
            harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.DISCARD);
            harness.handleCardChosen(player2, 0);

            assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
            assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

            harness.handleListChoice(player2, "Lose 4 life");
            harness.assertLife(player2, 16);
            harness.assertInGraveyard(player2, "Fountainport Bell");
            assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        });
    }

    @Test
    @DisplayName("Attack trigger uses last known blight counters when Viper is returned to hand")
    void attackTriggerUsesCountersFromDepartedViper() {
        Permanent viper = harness.addToBattlefieldAndReturn(player1, new RottenmouthViper());
        viper.setCounterCount(CounterType.BLIGHT, 2);
        viper.setSummoningSick(false);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new IntoTheFloodMaw()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(viper)));
            harness.ensurePriority(player2);
            harness.castInstantWithGift(player2, 0, viper.getId(), false);
            resolveAllTriggers();

            harness.assertInHand(player1, "Rottenmouth Viper");
            harness.assertNotOnBattlefield(player1, "Rottenmouth Viper");
            harness.assertLife(player2, 12);
        });
    }
}
