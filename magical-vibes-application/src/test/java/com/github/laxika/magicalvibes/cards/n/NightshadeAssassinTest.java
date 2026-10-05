package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.d.DarkWithering;
import com.github.laxika.magicalvibes.cards.f.FungalReaches;
import com.github.laxika.magicalvibes.cards.h.HauntingHymn;
import com.github.laxika.magicalvibes.cards.h.HavenwoodWurm;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NightshadeAssassin.class, BenalishCavalry.class, DarkWithering.class, FungalReaches.class,
        HauntingHymn.class, HavenwoodWurm.class})
class NightshadeAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature -X/-X for the number of selected black cards")
    void etbGivesTargetCreatureMinusForSelectedBlackCards() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new HavenwoodWurm());
        DarkWithering blackCard = new DarkWithering();
        harness.setHand(player1, List.of(new NightshadeAssassin(), blackCard));
        addCreatureMana();

        harness.castCreature(player1, 0, 0, wurm.getId());
        resolveAllTriggers();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice)
                        gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(blackCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(blackCard.getId()));

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Counts every selected black card and ignores nonblack cards")
    void countsEverySelectedBlackCard() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new HavenwoodWurm());
        DarkWithering firstBlackCard = new DarkWithering();
        DarkWithering secondBlackCard = new DarkWithering();
        FungalReaches nonblackCard = new FungalReaches();
        harness.setHand(player1, List.of(new NightshadeAssassin(), firstBlackCard, secondBlackCard,
                nonblackCard));
        addCreatureMana();

        harness.castCreature(player1, 0, 0, wurm.getId());
        resolveAllTriggers();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice)
                        gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(firstBlackCard.getId(), secondBlackCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstBlackCard.getId(), secondBlackCard.getId()));

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Allows revealing zero black cards")
    void allowsRevealingZeroBlackCards() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new HavenwoodWurm());
        harness.setHand(player1, List.of(new NightshadeAssassin(), new FungalReaches()));
        addCreatureMana();

        harness.castCreature(player1, 0, 0, wurm.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(6);
    }

    @Test
    @DisplayName("Revealing only some eligible cards leaves every card in hand")
    void mayRevealOnlySomeEligibleCards() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new HavenwoodWurm());
        DarkWithering revealed = new DarkWithering();
        HauntingHymn unrevealed = new HauntingHymn();
        harness.setHand(player1, List.of(new NightshadeAssassin(), revealed, unrevealed));
        addCreatureMana();

        harness.castCreature(player1, 0, 0, wurm.getId());
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(revealed.getId()));

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed, unrevealed);
    }

    @Test
    @DisplayName("Reducing a creature's toughness to zero puts it into the graveyard")
    void lethalToughnessReductionKillsTarget() {
        harness.addToBattlefield(player2, new BenalishCavalry());
        DarkWithering first = new DarkWithering();
        HauntingHymn second = new HauntingHymn();
        harness.setHand(player1, List.of(new NightshadeAssassin(), first, second));
        addCreatureMana();

        harness.castCreature(player1, 0, 0, harness.getPermanentId(player2, "Benalish Cavalry"));
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        harness.assertNotOnBattlefield(player2, "Benalish Cavalry");
        harness.assertInGraveyard(player2, "Benalish Cavalry");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("May reveal zero black cards even when eligible cards are available")
    void mayRevealZeroBlackCards() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new HavenwoodWurm());
        DarkWithering blackCard = new DarkWithering();
        harness.setHand(player1, List.of(new NightshadeAssassin(), blackCard));
        addCreatureMana();

        harness.castCreature(player1, 0, 0, wurm.getId());
        resolveAllTriggers();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice)
                        gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(blackCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(6);
    }

    @Test
    @DisplayName("Does not reveal cards when the ETB target is illegal on resolution")
    void doesNotRevealWhenTargetLeavesBeforeResolution() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new HavenwoodWurm());
        DarkWithering blackCard = new DarkWithering();
        harness.setHand(player1, List.of(new NightshadeAssassin(), blackCard));
        addCreatureMana();

        harness.castCreature(player1, 0, 0, wurm.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(wurm);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(blackCard);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new FungalReaches());
        harness.setHand(player1, List.of(new NightshadeAssassin(), new DarkWithering()));
        addCreatureMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discarding Nightshade Assassin offers its madness cost")
    void discardTriggersMadness() {
        NightshadeAssassin assassin = discardAssassin();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(assassin.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting madness casts Nightshade Assassin")
    void acceptingMadnessCastsCreature() {
        NightshadeAssassin assassin = discardAssassin();
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new HavenwoodWurm());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wurm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(assassin.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining madness puts Nightshade Assassin into its owner's graveyard")
    void decliningMadnessPutsAssassinInGraveyard() {
        NightshadeAssassin assassin = discardAssassin();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(assassin);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(assassin.getId()));
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new HavenwoodWurm());
        DarkWithering blackCard = new DarkWithering();
        harness.setHand(player1, List.of(new NightshadeAssassin(), blackCard));
        addCreatureMana();

        harness.castCreature(player1, 0, 0, wurm.getId());
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(blackCard.getId()));

        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(5);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(6);
    }

    @Test
    @DisplayName("First strike lets Nightshade Assassin survive combat with a 2/2 blocker")
    void firstStrikeResolvesBeforeRegularDamage() {
        Permanent attacker = addCreatureReady(player1, new NightshadeAssassin());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new BenalishCavalry());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Nightshade Assassin");
        harness.assertInGraveyard(player2, "Benalish Cavalry");
    }

    private NightshadeAssassin discardAssassin() {
        NightshadeAssassin assassin = new NightshadeAssassin();
        harness.setHand(player1, List.of(assassin));
        harness.setHand(player2, List.of(new HauntingHymn()));
        harness.addMana(player2, ManaColor.BLACK, 6);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        return assassin;
    }

    private void addCreatureMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
