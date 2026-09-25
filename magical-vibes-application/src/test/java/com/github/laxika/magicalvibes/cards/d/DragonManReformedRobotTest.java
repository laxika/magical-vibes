package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CoalitionRelic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonManReformedRobot.class, CoalitionRelic.class, Divination.class,
        Forest.class, HillGiant.class, WrathOfGod.class})
class DragonManReformedRobotTest extends BaseCardTest {

    @Test
    @DisplayName("Power is the greatest noncreature mana value among the battlefield and graveyard")
    void powerUsesGreatestNoncreatureManaValue() {
        Permanent dragonMan = addCreatureReady(player1, new DragonManReformedRobot());
        harness.addToBattlefield(player1, new CoalitionRelic());
        harness.setGraveyard(player1, List.of(new Divination(), new HillGiant()));
        harness.setGraveyard(player2, List.of(new WrathOfGod()));

        assertThat(gqs.getEffectivePower(gd, dragonMan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dragonMan)).isEqualTo(5);

        harness.setGraveyard(player1, List.of(new WrathOfGod(), new HillGiant()));

        assertThat(gqs.getEffectivePower(gd, dragonMan)).isEqualTo(4);
    }

    @Test
    @DisplayName("Can be cast from the graveyard by discarding a card")
    void canBeCastFromGraveyardByDiscardingCard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new DragonManReformedRobot()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playFlashbackSpell(gd, player1, 0, null, null, List.of(), null, null, List.of(), 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof DragonManReformedRobot);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof Forest);
    }

    @Test
    @DisplayName("Cannot be cast from the graveyard without a card to discard")
    void cannotBeCastFromGraveyardWithoutDiscardingCard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new DragonManReformedRobot()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card");
    }
}
