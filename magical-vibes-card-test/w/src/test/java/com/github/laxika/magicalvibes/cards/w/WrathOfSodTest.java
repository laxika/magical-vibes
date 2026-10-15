package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BronzeplateBoar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SelesnyaGuildmage;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WrathOfSod.class, GrizzlyBears.class, Ornithopter.class, SelesnyaGuildmage.class, BronzeplateBoar.class})
class WrathOfSodTest extends BaseCardTest {

    @Test
    @DisplayName("puts manabond counters on all creatures and turns them into colored lands")
    void manabondsAllCreatures() {
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());

        resolveWrath();

        assertThat(ownBears.getCounterCount(CounterType.MANABOND)).isOne();
        assertThat(opposingBears.getCounterCount(CounterType.MANABOND)).isOne();
        assertThat(gqs.isLand(gd, ownBears)).isTrue();
        assertThat(gqs.isCreature(gd, ownBears)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, ownBears, CardSubtype.BEAR)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, ownBears)).containsExactly(CardColor.GREEN);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ownBears), null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isOne();
    }

    @Test
    @DisplayName("the manabond effect ends when the counter is removed")
    void manabondIsCounterBound() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        resolveWrath();

        bears.setCounterCount(CounterType.MANABOND, 0);

        assertThat(gqs.isLand(gd, bears)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.BEAR)).isTrue();
    }

    private void resolveWrath() {
        harness.castFromHand(player1, new WrathOfSod(), "{2}{G}{W}");
        harness.passBothPriorities();
    }

    @Test
    void colorlessManabondedCreatureLosesFlyingAndProducesNoMana() {
        Permanent thopter = addCreatureReady(player1, new Ornithopter());
        resolveWrath();

        assertThat(gqs.isLand(gd, thopter)).isTrue();
        assertThat(gqs.isCreature(gd, thopter)).isFalse();
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, thopter, CardSubtype.THOPTER)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(thopter), null, null);

        assertThat(thopter.isTapped()).isTrue();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
    }

    @Test
    void multicoloredManabondedCreatureProducesOneChosenColor() {
        Permanent guildmage = addCreatureReady(player1, new SelesnyaGuildmage());
        resolveWrath();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guildmage), null, null);
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isOne();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void manabondRemovesArtifactTypesAndEquipmentSubtypes() {
        Permanent boar = addCreatureReady(player1, new BronzeplateBoar());
        resolveWrath();

        assertThat(gqs.isLand(gd, boar)).isTrue();
        assertThat(gqs.isArtifact(gd, boar)).isFalse();
        assertThat(gqs.isCreature(gd, boar)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, boar, CardSubtype.BOAR)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, boar, CardSubtype.EQUIPMENT)).isFalse();
        assertThat(gqs.hasKeyword(gd, boar, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void creaturesEnteringLaterAreUnaffected() {
        resolveWrath();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(bears.getCounterCount(CounterType.MANABOND)).isZero();
        assertThat(gqs.isCreature(gd, bears)).isTrue();
        assertThat(gqs.isLand(gd, bears)).isFalse();
    }

    @Test
    void effectPersistsUntilTheLastManabondCounterIsRemoved() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        resolveWrath();
        bears.setCounterCount(CounterType.MANABOND, 2);
        bears.setCounterCount(CounterType.MANABOND, 1);

        assertThat(gqs.isLand(gd, bears)).isTrue();
        assertThat(gqs.isCreature(gd, bears)).isFalse();

        bears.setCounterCount(CounterType.MANABOND, 0);

        assertThat(gqs.isCreature(gd, bears)).isTrue();
        assertThat(gqs.isLand(gd, bears)).isFalse();
    }
}
