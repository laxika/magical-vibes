package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({GrizzlyBears.class, KrosanVerge.class, LeadAstray.class, SuntailHawk.class})
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
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent unchosenCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castLeadAstrayForJudReview(List.of(ownCreature.getId(), opponentCreature.getId()));

        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(unchosenCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot choose more than two target creatures")
    void cannotChooseMoreThanTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castLeadAstrayForJudReview(List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castLeadAstrayForJudReview() {
        harness.setHand(player1, List.of(new LeadAstray()));
        addMana();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void castLeadAstrayForJudReview(List<java.util.UUID> targets) {
        harness.setHand(player1, List.of(new LeadAstray()));
        addMana();
        harness.castInstant(player1, 0, targets);
        harness.passBothPriorities();
    }
}
