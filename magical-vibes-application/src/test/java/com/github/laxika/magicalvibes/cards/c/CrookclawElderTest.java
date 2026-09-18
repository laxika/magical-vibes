package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrookclawElder.class, AvenEnvoy.class, FugitiveWizard.class})
class CrookclawElderTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping two Birds draws a card")
    void tappingTwoBirdsDrawsCard() {
        Permanent elder = addCreatureReady(player1, new CrookclawElder());
        Permanent bird1 = addCreatureReady(player1, new AvenEnvoy());
        Permanent bird2 = addCreatureReady(player1, new AvenEnvoy());
        FugitiveWizard drawnCard = new FugitiveWizard();
        harness.setLibrary(player1, List.of(drawnCard));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, battlefieldIndex(player1, elder), 0, null, null);
        harness.handlePermanentChosen(player1, bird1.getId());
        harness.handlePermanentChosen(player1, bird2.getId());
        harness.passBothPriorities();

        assertThat(bird1.isTapped()).isTrue();
        assertThat(bird2.isTapped()).isTrue();
        assertThat(elder.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(handBefore + 1)
                .contains(drawnCard);
    }

    @Test
    @DisplayName("The source can be one of the two Birds tapped as a cost")
    void sourceCanBeTappedAsBirdCost() {
        Permanent elder = addCreatureReady(player1, new CrookclawElder());
        Permanent bird = addCreatureReady(player1, new AvenEnvoy());
        Permanent spareBird = addCreatureReady(player1, new AvenEnvoy());
        FugitiveWizard drawnCard = new FugitiveWizard();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, battlefieldIndex(player1, elder), 0, null, null);
        harness.handlePermanentChosen(player1, elder.getId());
        harness.handlePermanentChosen(player1, bird.getId());
        harness.passBothPriorities();

        assertThat(elder.isTapped()).isTrue();
        assertThat(bird.isTapped()).isTrue();
        assertThat(spareBird.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Tapping two Wizards gives a target creature flying until end of turn")
    void tappingTwoWizardsGivesTargetFlying() {
        Permanent elder = addCreatureReady(player1, new CrookclawElder());
        Permanent wizard1 = addCreatureReady(player1, new FugitiveWizard());
        Permanent wizard2 = addCreatureReady(player1, new FugitiveWizard());
        Permanent target = addCreatureReady(player2, new FugitiveWizard());

        harness.activateAbility(player1, battlefieldIndex(player1, elder), 1, null, target.getId());
        harness.handlePermanentChosen(player1, wizard1.getId());
        harness.handlePermanentChosen(player1, wizard2.getId());
        harness.passBothPriorities();

        assertThat(wizard1.isTapped()).isTrue();
        assertThat(wizard2.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The granted flying wears off at cleanup")
    void grantedFlyingWearsOffAtCleanup() {
        Permanent elder = addCreatureReady(player1, new CrookclawElder());
        Permanent wizard1 = addCreatureReady(player1, new FugitiveWizard());
        Permanent wizard2 = addCreatureReady(player1, new FugitiveWizard());
        Permanent target = addCreatureReady(player2, new FugitiveWizard());

        harness.activateAbility(player1, battlefieldIndex(player1, elder), 1, null, target.getId());
        harness.handlePermanentChosen(player1, wizard1.getId());
        harness.handlePermanentChosen(player1, wizard2.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Tapped matching creatures cannot pay the tap cost")
    void tappedMatchingCreatureCannotPayCost() {
        Permanent elder = addCreatureReady(player1, new CrookclawElder());
        Permanent bird = addCreatureReady(player1, new AvenEnvoy());
        bird.tap();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, elder), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each ability requires two matching untapped creatures")
    void abilitiesRequireTwoMatchingCreatures() {
        Permanent elder = addCreatureReady(player1, new CrookclawElder());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, elder), 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        addCreatureReady(player1, new AvenEnvoy());
        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, elder), 1, null, elder.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
