package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpiketailHatchling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IsaoEnlightenedBushiTest extends BaseCardTest {

    @Test
    void cannotBeCountered() {
        IsaoEnlightenedBushi isao = new IsaoEnlightenedBushi();
        harness.setHand(player1, List.of(isao));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.addToBattlefield(player2, new SpiketailHatchling());
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, isao.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(isao.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(isao.getId()));
    }

    @Test
    void bushidoTriggersWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent isao = addCreatureReady(player2, new IsaoEnlightenedBushi());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, isao)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, isao)).isEqualTo(3);
    }

    @Test
    void bushidoTriggersWhenBecomesBlocked() {
        Permanent isao = addCreatureReady(player1, new IsaoEnlightenedBushi());
        isao.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, isao)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, isao)).isEqualTo(3);
    }

    @Test
    void regeneratesTargetSamurai() {
        harness.addToBattlefield(player1, new IsaoEnlightenedBushi());
        Permanent samurai = addCreatureReady(player2, createSamurai());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, samurai.getId());
        harness.passBothPriorities();

        assertThat(samurai.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void cannotRegenerateNonSamurai() {
        harness.addToBattlefield(player1, new IsaoEnlightenedBushi());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Samurai");
    }

    private Card createSamurai() {
        Card card = new Card();
        card.setName("Samurai");
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(CardSubtype.SAMURAI));
        return card;
    }
}
