package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CoastalDrake;
import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.i.Index;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JadedResponse.class, CoastalDrake.class, Dodecapod.class, Index.class})
class JadedResponseTest extends BaseCardTest {

    @Test
    void countersSpellSharingColorWithControlledCreature() {
        harness.addToBattlefield(player2, new CoastalDrake());

        CoastalDrake target = new CoastalDrake();
        harness.castFromHand(player1, target, "{2}{U}");

        harness.setHand(player2, List.of(new JadedResponse()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(se -> se.getCard().getId().equals(target.getId()));
        harness.assertInGraveyard(player1, "Coastal Drake");
        harness.assertInGraveyard(player2, "Jaded Response");
    }

    @Test
    void doesNotCounterColorlessSpell() {
        harness.addToBattlefield(player2, new CoastalDrake());

        Dodecapod target = new Dodecapod();
        harness.castFromHand(player1, target, "{4}");

        harness.setHand(player2, List.of(new JadedResponse()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dodecapod");
        harness.assertInGraveyard(player2, "Jaded Response");
    }

    @Test
    void countersNoncreatureSpellSharingColorWithControlledCreature() {
        harness.addToBattlefield(player2, new CoastalDrake());

        Index target = new Index();
        harness.castFromHand(player1, target, "{U}");

        harness.setHand(player2, List.of(new JadedResponse()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(se -> se.getCard().getId().equals(target.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Index");
        harness.assertInGraveyard(player2, "Jaded Response");
    }

    @Test
    void evaluatesControlledCreaturesWhenResponseResolves() {
        Index target = new Index();
        harness.castFromHand(player1, target, "{U}");

        harness.setHand(player2, List.of(new JadedResponse()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, target.getId());

        harness.addToBattlefield(player2, new CoastalDrake());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(se -> se.getCard().getId().equals(target.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Index");
        harness.assertOnBattlefield(player2, "Coastal Drake");
        harness.assertInGraveyard(player2, "Jaded Response");
    }
}
