package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AdarkarWastes;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({MyriadConstruct.class, AdarkarWastes.class, Forest.class, ProdigalPyromancer.class, Shock.class})
class MyriadConstructTest extends BaseCardTest {

    @Test
    void kickedEntersWithCountersForOpponentsNonbasicLands() {
        harness.addToBattlefield(player2, new AdarkarWastes());
        harness.addToBattlefield(player2, new AdarkarWastes());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new MyriadConstruct()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent myriad = findPermanent(player1, "Myriad Construct");
        assertThat(myriad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(myriad.getEffectivePower()).isEqualTo(6);
    }

    @Test
    void becomingTargetOfSpellSacrificesItAndCreatesTokensEqualToItsPower() {
        Permanent myriad = harness.addToBattlefieldAndReturn(player1, new MyriadConstruct());
        myriad.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, myriad.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Myriad Construct");
        List<Permanent> constructs = findPermanents(player1, "Construct");
        assertThat(constructs).hasSize(6);
        assertThat(constructs).allSatisfy(construct -> {
            assertThat(construct.getCard().getSubtypes()).containsExactly(CardSubtype.CONSTRUCT);
            assertThat(construct.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(construct.getEffectivePower()).isEqualTo(1);
            assertThat(construct.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    void becomingTargetOfAbilityDoesNotTriggerIt() {
        Permanent myriad = harness.addToBattlefieldAndReturn(player1, new MyriadConstruct());
        harness.addToBattlefield(player2, new ProdigalPyromancer());
        Permanent pyromancer = findPermanent(player2, "Prodigal Pyromancer");
        pyromancer.setSummoningSick(false);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer),
                null, myriad.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(myriad);
        assertThat(findPermanents(player1, "Construct")).isEmpty();
    }
}
