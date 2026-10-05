package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LlanowarMentor.class, LlanowarAugur.class})
class LlanowarMentorTest extends BaseCardTest {

    @Test
    void discardingACardCreatesLlanowarElvesToken() {
        Permanent mentor = addReadyMentor();
        harness.setHand(player1, List.of(new LlanowarAugur()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(mentor.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.assertNotInHand(player1, "Llanowar Augur");
        harness.assertInGraveyard(player1, "Llanowar Augur");

        Permanent token = findPermanent(player1, "Llanowar Elves");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getName()).isEqualTo("Llanowar Elves");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ELF, CardSubtype.DRUID);
        assertThat(token.isSummoningSick()).isTrue();
    }

    @Test
    void LlanowarElvesTokenCanTapForGreenMana() {
        Permanent token = createLlanowarElvesToken();
        token.setSummoningSick(false);
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);

        harness.activateAbility(player1, tokenIndex, 0, null, null);

        assertThat(token.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void LlanowarElvesTokenCannotTapForGreenManaWithSummoningSickness() {
        Permanent token = createLlanowarElvesToken();
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);

        assertThatThrownBy(() -> harness.activateAbility(player1, tokenIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(token.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new LlanowarMentor());
        harness.setHand(player1, List.of(new LlanowarAugur()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(mentor.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void cannotActivateWithoutACardToDiscard() {
        Permanent mentor = addReadyMentor();
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mentor.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void paysCostsBeforeCreatingTokenOnResolution() {
        Permanent mentor = addReadyMentor();
        harness.setHand(player1, List.of(new LlanowarAugur(), new LlanowarMentor()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 1);

        assertThat(mentor.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.assertInGraveyard(player1, "Llanowar Mentor");
        harness.assertInHand(player1, "Llanowar Augur");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Llanowar Elves")).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Llanowar Elves")).isEqualTo(1);
        assertThat(findPermanent(player1, "Llanowar Elves").isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void cannotActivateWithoutGreenMana() {
        Permanent mentor = addReadyMentor();
        harness.setHand(player1, List.of(new LlanowarAugur()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mentor.isTapped()).isFalse();
        harness.assertInHand(player1, "Llanowar Augur");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhenAlreadyTapped() {
        Permanent mentor = addReadyMentor();
        mentor.setTapped(true);
        harness.setHand(player1, List.of(new LlanowarAugur()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Llanowar Augur");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyMentor() {
        return addCreatureReady(player1, new LlanowarMentor());
    }

    private Permanent createLlanowarElvesToken() {
        addReadyMentor();
        harness.setHand(player1, List.of(new LlanowarAugur()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Llanowar Elves");
    }
}
