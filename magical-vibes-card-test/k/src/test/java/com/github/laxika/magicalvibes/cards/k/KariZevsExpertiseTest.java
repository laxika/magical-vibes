package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SleekSchooner;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({KariZevsExpertise.class, GrizzlyBears.class, SerraAngel.class, SleekSchooner.class, FountainOfYouth.class})
class KariZevsExpertiseTest extends BaseCardTest {

    @Test
    @DisplayName("Steals, untaps and hastes a target creature")
    void stealsUntapsAndHastesCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();

        castExpertise(target, List.of(new KariZevsExpertise()));

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Can target a noncreature Vehicle")
    void targetsVehicle() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SleekSchooner());
        target.tap();

        castExpertise(target, List.of(new KariZevsExpertise()));

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castExpertise(target, List.of(new KariZevsExpertise()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Offers a spell with mana value two or less from hand for free")
    void castsLowManaValueSpellFromHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears freeSpell = new GrizzlyBears();

        castExpertise(target, List.of(new KariZevsExpertise(), freeSpell));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(freeSpell.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(freeSpell.getId()));
    }

    @Test
    @DisplayName("Does not offer a spell with mana value greater than two")
    void doesNotOfferHighManaValueSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        SerraAngel highManaValueSpell = new SerraAngel();

        castExpertise(target, List.of(new KariZevsExpertise(), highManaValueSpell));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(highManaValueSpell);
    }

    @Test
    @DisplayName("Cannot target a noncreature non-Vehicle permanent")
    void cannotTargetOtherPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new KariZevsExpertise()));
        addExpertiseMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle");
    }

    @Test
    @DisplayName("Can target an untapped creature already controlled by the caster")
    void targetsOwnUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castExpertise(target, List.of(new KariZevsExpertise()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Declining the free spell does not undo control, untapping or haste")
    void canDeclineFreeSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        GrizzlyBears freeSpell = new GrizzlyBears();

        castExpertise(target, List.of(new KariZevsExpertise(), freeSpell));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(freeSpell);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Can cast a zero-mana noncreature spell")
    void castsZeroManaArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        FountainOfYouth freeSpell = new FountainOfYouth();

        castExpertise(target, List.of(new KariZevsExpertise(), freeSpell));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(freeSpell.getId()));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(freeSpell);
    }

    @Test
    @DisplayName("Only one spell can be cast when multiple hand cards are eligible")
    void castsOnlyOneEligibleSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears firstSpell = new GrizzlyBears();
        FountainOfYouth secondSpell = new FountainOfYouth();

        castExpertise(target, List.of(new KariZevsExpertise(), firstSpell, secondSpell));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(firstSpell.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(secondSpell.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(secondSpell).doesNotContain(firstSpell);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("An illegal target prevents the free spell from being offered")
    void noFreeSpellWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears freeSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(new KariZevsExpertise(), freeSpell));
        addExpertiseMana();
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(freeSpell);
        harness.assertInGraveyard(player1, "Kari Zev's Expertise");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    private void castExpertise(Permanent target, List<com.github.laxika.magicalvibes.model.Card> hand) {
        harness.setHand(player1, hand);
        addExpertiseMana();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addExpertiseMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
