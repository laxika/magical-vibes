package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TooEvilToStayDead.class, CrawWurm.class, GrizzlyBears.class, HillGiant.class})
class TooEvilToStayDeadTest extends BaseCardTest {

    @Test
    void returnsCreatureWithManaValueFourOrLessWithoutTeamwork() {
        Card target = new GrizzlyBears();

        cast(target, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == target);
    }

    @Test
    void cannotTargetCreatureWithManaValueAboveFourWithoutTeamwork() {
        Card target = new CrawWurm();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new TooEvilToStayDead()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 4 or less");
    }

    @Test
    void returnsCreatureWithAnyManaValueWhenTeamworkIsPaid() {
        Card target = new CrawWurm();
        Permanent firstTeammate = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondTeammate = addCreatureReady(player1, new GrizzlyBears());

        cast(target, List.of(firstTeammate.getId(), secondTeammate.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == target);
        assertThat(firstTeammate.isTapped()).isTrue();
        assertThat(secondTeammate.isTapped()).isTrue();
    }

    private void cast(Card target, List<java.util.UUID> teamworkPermanents) {
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new TooEvilToStayDead()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorceryWithSacrifices(player1, 0, target.getId(), teamworkPermanents);
        harness.passBothPriorities();
    }

    @Test
    void returnsCreatureWithManaValueExactlyFourWithoutTeamwork() {
        Card target = new HillGiant();

        cast(target, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void canTapSummoningSickCreatureWithMoreThanFourPowerForTeamwork() {
        Card target = new CrawWurm();
        Permanent teammate = harness.addToBattlefieldAndReturn(player1, new CrawWurm());
        teammate.setSummoningSick(true);

        cast(target, List.of(teammate.getId()));

        assertThat(teammate.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void availableTeamworkDoesNotUpgradeSpellUnlessPaid() {
        Permanent teammate = addCreatureReady(player1, new CrawWurm());
        Card target = new CrawWurm();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new TooEvilToStayDead()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(teammate.isTapped()).isFalse();
    }

    @Test
    void cannotPayTeamworkWithInsufficientTotalPower() {
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());
        Card target = new GrizzlyBears();

        assertThatThrownBy(() -> cast(target, List.of(teammate.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(teammate.isTapped()).isFalse();
    }

    @Test
    void cannotCountSameCreatureTwiceForTeamwork() {
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> cast(new GrizzlyBears(),
                List.of(teammate.getId(), teammate.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(teammate.isTapped()).isFalse();
    }

    @Test
    void cannotTapOpponentsCreatureForTeamwork() {
        Permanent teammate = addCreatureReady(player2, new CrawWurm());

        assertThatThrownBy(() -> cast(new GrizzlyBears(), List.of(teammate.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(teammate.isTapped()).isFalse();
    }

    @Test
    void cannotTapAlreadyTappedCreatureForTeamwork() {
        Permanent teammate = addCreatureReady(player1, new CrawWurm());
        teammate.tap();

        assertThatThrownBy(() -> cast(new GrizzlyBears(), List.of(teammate.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNoncreatureCardEvenWithTeamwork() {
        Permanent teammate = addCreatureReady(player1, new CrawWurm());

        assertThatThrownBy(() -> cast(new TooEvilToStayDead(), List.of(teammate.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetCreatureInOpponentsGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new TooEvilToStayDead()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReturnTargetThatLeavesGraveyardBeforeResolution() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new TooEvilToStayDead()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(target.getId())).isNotNull();
    }
}
