package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.h.HeartsDesire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LovestruckBeast.class, HeartsDesire.class, RagingGoblin.class, GrizzlyBears.class})
class LovestruckBeastTest extends BaseCardTest {

    @Test
    void adventureCreatesA1x1WhiteHumanAndExilesTheCard() {
        LovestruckBeast card = new LovestruckBeast();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent human = findPermanent(player1, "Human");
        assertThat(human.getCard().isToken()).isTrue();
        assertThat(human.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(human.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(human.getEffectivePower()).isEqualTo(1);
        assertThat(human.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void cannotAttackWithoutAControlled1x1Creature() {
        addCreatureReady(player1, new LovestruckBeast());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentControlled1x1CreatureDoesNotEnableAttack() {
        addCreatureReady(player1, new LovestruckBeast());
        addCreatureReady(player2, new RagingGoblin());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void controlled2x2CreatureDoesNotEnableAttack() {
        addCreatureReady(player1, new LovestruckBeast());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canAttackWithAControlled1x1Creature() {
        addCreatureReady(player1, new LovestruckBeast());
        addCreatureReady(player1, new RagingGoblin());

        assertThatCode(() -> declareAttackers(player1, List.of(0)))
                .doesNotThrowAnyException();
    }
}
