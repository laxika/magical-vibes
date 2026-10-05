package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CopperHostCrusher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GitaxianSpellstalker;
import com.github.laxika.magicalvibes.cards.k.KhenraSpellspear;
import com.github.laxika.magicalvibes.cards.v.VertexPaladin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CopperHostCrusher.class, GitaxianSpellstalker.class, GrizzlyBears.class,
        InvasionOfXerex.class, KhenraSpellspear.class, VertexPaladin.class})
class InvasionOfXerexTest extends BaseCardTest {

    @Test
    void returnsOptionalTargetCreatureToItsOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castInvasion(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
    }

    @Test
    void canEnterWithoutChoosingTheOptionalTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castInvasion(null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof InvasionOfXerex);
    }

    @Test
    void defeatingTheSiegeCastsVertexPaladinWithCreatureCountPowerAndToughness() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfXerex());
        battle.setCounterCount(CounterType.DEFENSE, 0);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent paladin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof VertexPaladin)
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, paladin)).isEqualTo(3);

        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, paladin)).isEqualTo(4);
    }

    @Test
    void canReturnItsControllersHexproofCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CopperHostCrusher());

        castInvasion(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player1.getId())).contains(target.getCard());
    }

    @Test
    void doesNotReturnATargetThatLeftBeforeTheTriggerResolved() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CopperHostCrusher());

        castInvasion(target.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(target.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof InvasionOfXerex);
    }

    @Test
    void paladinCountsItselfAndOnlyItsControllersCreaturesAndShrinksWhenTheyLeave() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new CopperHostCrusher());
        harness.addToBattlefield(player2, new CopperHostCrusher());
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new VertexPaladin());

        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, paladin)).isEqualTo(2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ally));

        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, paladin)).isEqualTo(1);
    }

    @Test
    void castingVertexPaladinFromTheDefeatedSiegeDoesNotTriggerProwess() {
        Permanent spellspear = harness.addToBattlefieldAndReturn(player1, new KhenraSpellspear());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfXerex());
        battle.setCounterCount(CounterType.DEFENSE, 0);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spellspear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spellspear)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof VertexPaladin);
    }

    private void castInvasion(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new InvasionOfXerex()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }
}
