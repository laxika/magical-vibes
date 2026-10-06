package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RotFarmMortipede.class, Disentomb.class, GrizzlyBears.class, Recollect.class,
        Reminisce.class, Shock.class})
class RotFarmMortipedeTest extends BaseCardTest {

    @Test
    void getsBoostAndKeywordsWhenCreatureLeavesYourGraveyard() {
        Permanent mortipede = addMortipede();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(mortipede.getPowerModifier()).isEqualTo(1);
        assertThat(mortipede.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, mortipede, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, mortipede, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void doesNotTriggerWhenNoncreatureCardLeavesYourGraveyard() {
        Permanent mortipede = addMortipede();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new Recollect()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, shock.getId());

        assertThat(mortipede.getPowerModifier()).isEqualTo(0);
        assertThat(mortipede.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, mortipede, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, mortipede, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void triggersOnlyOnceWhenSeveralCreatureCardsLeaveTogether() {
        Permanent mortipede = addMortipede();
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(mortipede.getPowerModifier()).isEqualTo(1);
        assertThat(mortipede.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, mortipede, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, mortipede, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void boostAndKeywordsWearOffAtEndOfTurn() {
        Permanent mortipede = addMortipede();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mortipede.getPowerModifier()).isEqualTo(0);
        assertThat(mortipede.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, mortipede, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, mortipede, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void doesNotTriggerWhenCreaturesLeaveOpponentsGraveyard() {
        Permanent mortipede = addMortipede();
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(mortipede.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, mortipede, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, mortipede, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void separateDeparturesEachGiveAnotherPowerBoost() {
        Permanent mortipede = addMortipede();
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Disentomb(), new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, first.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.castAndResolveSorcery(player1, 0, second.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(mortipede.getPowerModifier()).isEqualTo(2);
        assertThat(mortipede.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, mortipede, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, mortipede, Keyword.LIFELINK)).isTrue();
    }

    private Permanent addMortipede() {
        return harness.addToBattlefieldAndReturn(player1, new RotFarmMortipede());
    }
}
