package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MeagerMeal;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GollumSilentSlinker.class, MeagerMeal.class, GrizzlyBears.class})
class GollumSilentSlinkerTest extends BaseCardTest {

    @Test
    void adventurePutsCounterOnCreatureAndGivesTargetPlayerLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GollumSilentSlinker card = new GollumSilentSlinker();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAdventure(player1, 0, List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 22);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCanOmitCreatureTarget() {
        GollumSilentSlinker card = new GollumSilentSlinker();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAdventure(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 22);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        GollumSilentSlinker card = new GollumSilentSlinker();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAdventure(player1, 0, List.of(player1.getId()));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gollum, Silent Slinker");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }
}
