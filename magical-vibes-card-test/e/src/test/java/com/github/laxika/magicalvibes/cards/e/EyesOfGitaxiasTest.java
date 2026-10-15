package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FurtiveAnalyst;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EyesOfGitaxias.class, FurtiveAnalyst.class})
class EyesOfGitaxiasTest extends BaseCardTest {

    @Test
    void incubatesThreeAndDrawsACard() {
        harness.setHand(player1, List.of(new EyesOfGitaxias()));
        harness.setLibrary(player1, List.of(new FurtiveAnalyst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertInHand(player1, "Furtive Analyst");
    }

    @Test
    void createsANoncreatureIncubatorArtifact() {
        harness.setHand(player1, List.of(new EyesOfGitaxias()));
        harness.setLibrary(player1, List.of(new FurtiveAnalyst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(incubator.getCard().hasType(CardType.CREATURE)).isFalse();
        assertThat(incubator.getCard().getSubtypes()).contains(CardSubtype.INCUBATOR);
        assertThat(incubator.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void tokenTransformsIntoAThreeThreePhyrexianAndKeepsItsCounters() {
        harness.setHand(player1, List.of(new EyesOfGitaxias()));
        harness.setLibrary(player1, List.of(new FurtiveAnalyst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, List.of());
        Permanent incubator = findPermanent(player1, "Incubator");

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        assertThat(incubator.isTransformed()).isFalse();
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(incubator.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(incubator.getCard().getSubtypes()).containsExactly(CardSubtype.PHYREXIAN);
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(3);
        assertThat(incubator.getCard().getActivatedAbilities()).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(incubator);
    }
}
