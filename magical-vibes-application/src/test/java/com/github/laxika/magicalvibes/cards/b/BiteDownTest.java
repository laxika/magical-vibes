package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BiteDownTest extends BaseCardTest {

    @Test
    @DisplayName("The creature you control deals damage equal to its power to an opponent's creature")
    void dealsPowerDamageToOpponentCreature() {
        Permanent source = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(source);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BiteDown()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0,
                List.of(source.getId(), harness.getPermanentId(player2, "Grizzly Bears")));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The creature you control deals damage equal to its power to an opponent's planeswalker")
    void dealsPowerDamageToOpponentPlaneswalker() {
        Permanent source = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(source);
        Permanent planeswalker = new Permanent(new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);
        harness.setHand(player1, List.of(new BiteDown()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature you control as the victim")
    void cannotTargetOwnCreatureAsVictim() {
        Permanent source = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(source);
        Permanent victim = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(victim);
        harness.setHand(player1, List.of(new BiteDown()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use an opponent's creature as the damage source")
    void cannotUseOpponentsCreatureAsSource() {
        Permanent source = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player2.getId()).add(source);
        Permanent victim = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(victim);
        harness.setHand(player1, List.of(new BiteDown()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
