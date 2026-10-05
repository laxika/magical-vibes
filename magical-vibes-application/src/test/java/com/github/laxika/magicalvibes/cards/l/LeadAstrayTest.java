package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrosanVerge.class, LeadAstray.class, SuntailHawk.class})
class LeadAstrayTest extends BaseCardTest {

    @Test
    @DisplayName("Taps two target creatures")
    void tapsTwoTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        castLeadAstray(List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not tap an untargeted creature")
    void doesNotTapUntargetedCreature() {
        Permanent targeted = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent untargeted = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        castLeadAstray(List.of(targeted.getId()));

        assertThat(targeted.isTapped()).isTrue();
        assertThat(untargeted.isTapped()).isFalse();
    }

    @Test
    @DisplayName("May target one creature")
    void tapsOneTargetCreature() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        castLeadAstray(List.of(hawk.getId()));

        assertThat(hawk.isTapped()).isTrue();
    }

    @Test
    @DisplayName("May target no creatures")
    void mayTargetNoCreatures() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        castLeadAstray(List.of());

        assertThat(hawk.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());
        harness.setHand(player1, List.of(new LeadAstray()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castLeadAstray(List<UUID> targets) {
        harness.setHand(player1, List.of(new LeadAstray()));
        addMana();
        harness.castAndResolveInstant(player1, 0, targets);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Taps only the chosen creatures regardless of controller")
    void tapsOnlyChosenCreaturesRegardlessOfController() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent unchosenCreature = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        castLeadAstray(List.of(ownCreature.getId(), opponentCreature.getId()));

        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(unchosenCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot choose more than two target creatures")
    void cannotChooseMoreThanTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        assertThatThrownBy(() -> castLeadAstray(List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotChooseTheSameCreatureTwice() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new LeadAstray()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(hawk.getId(), hawk.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May target an already tapped creature")
    void mayTargetAlreadyTappedCreature() {
        Permanent tapped = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent untapped = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        tapped.setTapped(true);

        castLeadAstray(List.of(tapped.getId(), untapped.getId()));

        assertThat(tapped.isTapped()).isTrue();
        assertThat(untapped.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Lead Astray");
    }

    @Test
    @DisplayName("May be cast with no creatures on the battlefield")
    void mayBeCastOnEmptyBattlefield() {
        castLeadAstray(List.of());

        harness.assertInGraveyard(player1, "Lead Astray");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Still taps the remaining target when one target leaves before resolution")
    void tapsRemainingLegalTarget() {
        Permanent departing = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new LeadAstray()));
        addMana();
        harness.castInstant(player1, 0, List.of(departing.getId(), remaining.getId()));
        harness.getPermanentRemovalService().removePermanentToHand(gd, departing);

        harness.passBothPriorities();

        assertThat(remaining.isTapped()).isTrue();
        assertThat(departing.isTapped()).isFalse();
        harness.assertInHand(player2, "Suntail Hawk");
        harness.assertInGraveyard(player1, "Lead Astray");
    }
}
