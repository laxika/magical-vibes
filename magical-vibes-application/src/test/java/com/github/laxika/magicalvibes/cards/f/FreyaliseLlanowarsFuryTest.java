package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        FreyaliseLlanowarsFury.class,
        AngelicChorus.class,
        Forest.class,
        FountainOfYouth.class,
        GrizzlyBears.class,
        SuntailHawk.class
})
class FreyaliseLlanowarsFuryTest extends BaseCardTest {

    @Test
    void plusTwoCreatesElfDruidTokenThatAddsGreenMana() {
        Permanent freyalise = addReadyFreyalise(3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ELF, CardSubtype.DRUID);
        assertThat(freyalise.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);

        token.setSummoningSick(false);
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        harness.activateAbility(player1, tokenIndex, null, null);

        assertThat(token.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void minusTwoDestroysTargetArtifactOrEnchantment() {
        Permanent freyalise = addReadyFreyalise(3);
        harness.addToBattlefield(player2, new FountainOfYouth());

        Permanent artifact = findPermanent(player2, "Fountain of Youth");
        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(freyalise.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void minusTwoDestroysTargetEnchantment() {
        Permanent freyalise = addReadyFreyalise(3);
        harness.addToBattlefield(player2, new AngelicChorus());

        Permanent enchantment = findPermanent(player2, "Angelic Chorus");
        harness.activateAbility(player1, 0, 1, null, enchantment.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Angelic Chorus");
        assertThat(freyalise.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void minusTwoCannotTargetCreature() {
        addReadyFreyalise(3);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    @Test
    void minusSixDrawsForEachGreenCreatureYouControl() {
        addReadyFreyalise(6);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getName().equals("Freyalise, Llanowar's Fury"));
    }

    private Permanent addReadyFreyalise(int loyalty) {
        Permanent freyalise = new Permanent(new FreyaliseLlanowarsFury());
        freyalise.setCounterCount(CounterType.LOYALTY, loyalty);
        freyalise.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(freyalise);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        return freyalise;
    }
}
