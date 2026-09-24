package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinCrashPilot.class, DuskLegionDreadnought.class})
class GoblinCrashPilotTest extends BaseCardTest {

    @Test
    void crewsVehicleGivesItHasteThenSacrificesItAndDealsItsPowerAsDamage() {
        Permanent pilot = addReady(player1, new GoblinCrashPilot());
        Permanent dreadnought = addReady(player1, new DuskLegionDreadnought());

        harness.activateAbility(player1, indexOf(player1, dreadnought), null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dreadnought, Keyword.HASTE)).isTrue();
        assertThat(gqs.isCreature(gd, dreadnought)).isTrue();
        assertThat(pilot.isTapped()).isTrue();

        declareAttackers(player1, List.of());
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dusk Legion Dreadnought");
        harness.assertInGraveyard(player1, "Dusk Legion Dreadnought");
        harness.assertLife(player2, 16);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
