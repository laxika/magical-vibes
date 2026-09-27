package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnheloThePainter.class, GrizzlyBears.class, LightningBolt.class, LlanowarElves.class})
class AnheloThePainterTest extends BaseCardTest {

    @Test
    @DisplayName("The first instant or sorcery each turn has casualty 2")
    void firstInstantOrSorceryHasCasualtyTwo() {
        harness.addToBattlefield(player1, new AnheloThePainter());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithCasualty(player1, 0, player2.getId(), List.of(fodder.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(fodder.getId()));
    }

    @Test
    @DisplayName("Anhelo grants casualty only to the first instant or sorcery each turn")
    void casualtyIsLimitedToFirstSpellEachTurn() {
        harness.addToBattlefield(player1, new AnheloThePainter());
        Permanent firstFodder = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondFodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castWithCasualty(player1, 0, player2.getId(), List.of(firstFodder.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castWithCasualty(
                player1, 0, player2.getId(), List.of(secondFodder.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no matching casualty cost");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(secondFodder.getId()));
    }

    @Test
    @DisplayName("Casualty 2 cannot be paid with a creature below power two")
    void casualtyRequiresPowerTwo() {
        harness.addToBattlefield(player1, new AnheloThePainter());
        Permanent fodder = addCreatureReady(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithCasualty(
                player1, 0, player2.getId(), List.of(fodder.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(fodder.getId()));
    }
}
