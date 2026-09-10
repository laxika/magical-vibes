package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AgadeemsAwakening.class, AgadeemTheUndercrypt.class, Memnite.class,
        ElvishMystic.class, GrizzlyBears.class, SerraAngel.class})
class AgadeemsAwakeningTest extends BaseCardTest {

    @Test
    void returnsUpToOneCreatureOfEachManaValueAtMostX() {
        Card zeroManaCreature = new Memnite();
        Card oneManaCreature = new ElvishMystic();
        Card twoManaCreature = new GrizzlyBears();
        Card anotherTwoManaCreature = new GrizzlyBears();
        Card tooExpensiveCreature = new SerraAngel();
        harness.setGraveyard(player1, List.of(
                zeroManaCreature, oneManaCreature, twoManaCreature,
                anotherTwoManaCreature, tooExpensiveCreature));
        harness.setHand(player1, List.of(new AgadeemsAwakening()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 2, List.of());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                zeroManaCreature.getId(), oneManaCreature.getId(),
                twoManaCreature.getId(), anotherTwoManaCreature.getId());

        harness.handleMultipleCardsChosen(player1,
                List.of(zeroManaCreature.getId(), oneManaCreature.getId(), twoManaCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(
                        zeroManaCreature.getId(), oneManaCreature.getId(), twoManaCreature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(anotherTwoManaCreature, tooExpensiveCreature);
    }

    @Test
    void rejectsTwoChosenCreaturesWithTheSameManaValue() {
        Card firstTwoManaCreature = new GrizzlyBears();
        Card secondTwoManaCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstTwoManaCreature, secondTwoManaCreature));
        harness.setHand(player1, List.of(new AgadeemsAwakening()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 1, List.of());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(firstTwoManaCreature.getId(), secondTwoManaCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different mana values");
    }

    @Test
    void landFaceMayEnterUntappedForThreeLifeAndProducesBlackMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new AgadeemsAwakening()));

        gs.playCard(gd, player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(AgadeemTheUndercrypt.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(land.isTapped()).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void landFaceEntersTappedWhenLifePaymentIsDeclined() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AgadeemsAwakening()));

        gs.playCard(gd, player1, 0, 1, null, null);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }
}
