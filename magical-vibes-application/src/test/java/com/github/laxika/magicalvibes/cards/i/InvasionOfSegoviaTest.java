package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CaetusSeaTyrantOfSegovia;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WurmsTooth;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaetusSeaTyrantOfSegovia.class, GrizzlyBears.class, InvasionOfSegovia.class, WurmsTooth.class})
class InvasionOfSegoviaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and creates two 1/1 blue Kraken tokens with trample")
    void entersCreatesKrakenTokens() {
        harness.setHand(player1, List.of(new InvasionOfSegovia()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.KRAKEN);
            assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Defeating the Siege casts Caetus transformed")
    void defeatCastsCaetusTransformed() {
        Permanent caetus = addCaetus();

        assertThat(caetus.getCard()).isInstanceOf(CaetusSeaTyrantOfSegovia.class);
        assertThat(caetus.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Caetus gives noncreature spells convoke")
    void grantsConvokeToNoncreatureSpells() {
        addCaetus();
        Permanent convokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WurmsTooth()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(convokeCreature.getId()));

        assertThat(convokeCreature.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof WurmsTooth);
    }

    @Test
    @DisplayName("Caetus untaps up to four target creatures at your end step")
    void untapsUpToFourTargetCreatures() {
        addCaetus();
        List<Permanent> targets = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            creature.tap();
            targets.add(creature);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        for (int i = 0; i < 4; i++) {
            harness.handlePermanentChosen(player1, targets.get(i).getId());
        }
        harness.passBothPriorities();

        assertThat(targets.subList(0, 4)).allMatch(permanent -> !permanent.isTapped());
        assertThat(targets.get(4).isTapped()).isTrue();
    }

    private Permanent addCaetus() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfSegovia());
        battle.setCounterCount(CounterType.DEFENSE, 0);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof CaetusSeaTyrantOfSegovia)
                .findFirst()
                .orElseThrow();
    }
}
