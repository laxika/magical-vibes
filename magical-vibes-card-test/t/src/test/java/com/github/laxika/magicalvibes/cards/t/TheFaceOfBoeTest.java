package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.ProfaneTutor;
import com.github.laxika.magicalvibes.cards.p.Phthisis;
import com.github.laxika.magicalvibes.cards.b.BenalishCommander;
import com.github.laxika.magicalvibes.cards.j.JudoonEnforcers;
import com.github.laxika.magicalvibes.cards.s.Silence;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheFaceOfBoe.class, ProfaneTutor.class, JudoonEnforcers.class,
        Phthisis.class, BenalishCommander.class, Silence.class})
class TheFaceOfBoeTest extends BaseCardTest {

    @Test
    @DisplayName("Offers a suspended spell and casts it for its suspend cost")
    void castsSpellForSuspendCost() {
        TheFaceOfBoe faceOfBoe = new TheFaceOfBoe();
        ProfaneTutor tutor = new ProfaneTutor();
        addCreatureReady(player1, faceOfBoe);
        harness.setHand(player1, List.of(tutor));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(tutor);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == tutor);
    }

    @Test
    void decliningLeavesCardAndManaUntouched() {
        addCreatureReady(player1, new TheFaceOfBoe());
        ProfaneTutor tutor = new ProfaneTutor();
        harness.setHand(player1, List.of(tutor));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(tutor);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == tutor);
    }

    @Test
    void canDeclineFirstCardAndCastOnlyTheSecond() {
        addCreatureReady(player1, new TheFaceOfBoe());
        JudoonEnforcers first = new JudoonEnforcers();
        JudoonEnforcers second = new JudoonEnforcers();
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == second);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == first);
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void creatureEntersWithoutSuspendHasteOrTimeCounters() {
        addCreatureReady(player1, new TheFaceOfBoe());
        JudoonEnforcers enforcers = new JudoonEnforcers();
        harness.setHand(player1, List.of(enforcers));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        var permanent = findPermanent(player1, "Judoon Enforcers");
        assertThat(permanent.isSummoningSick()).isTrue();
        assertThat(permanent.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(permanent.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(enforcers);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void cannotActivateDuringUpkeep() {
        addCreatureReady(player1, new TheFaceOfBoe());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new TheFaceOfBoe());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void uncastableTargetedSpellStaysInHandWithoutPayment() {
        TheFaceOfBoe face = new TheFaceOfBoe();
        addCreatureReady(player1, face);
        Phthisis spell = new Phthisis();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, 0, null, null);

        // Model the source leaving the battlefield before its ability resolves.
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setGraveyard(player1, List.of(face));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void allowsChoosingPositiveXForSuspendCost() {
        addCreatureReady(player1, new TheFaceOfBoe());
        BenalishCommander commander = new BenalishCommander();
        harness.setHand(player1, List.of(commander));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).satisfies(interaction ->
                assertThat(interaction instanceof PendingInteraction.XValueChoice
                        || interaction instanceof PendingInteraction.AlternateCastXValueChoice).isTrue());
        harness.handleXValueChosen(player1, 3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == commander && entry.getXValue() == 3);
    }

    @Test
    void cannotCastWhileSilencedButCanActivateTheAbility() {
        addCreatureReady(player1, new TheFaceOfBoe());
        JudoonEnforcers enforcers = new JudoonEnforcers();
        harness.forceActivePlayer(player1);
        harness.castFromHand(player2, new Silence(), "{W}");
        harness.passBothPriorities();
        assertThat(gd.playersSilencedThisTurn).contains(player1.getId());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(enforcers));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).contains(enforcers);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == enforcers);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void insufficientManaLeavesTheSpellInHand() {
        addCreatureReady(player1, new TheFaceOfBoe());
        JudoonEnforcers enforcers = new JudoonEnforcers();
        harness.setHand(player1, List.of(enforcers));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).contains(enforcers);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == enforcers);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void doesNotOfferCardsWithoutSuspend() {
        addCreatureReady(player1, new TheFaceOfBoe());
        Silence spell = new Silence();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).contains(spell);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == spell);
    }
}
