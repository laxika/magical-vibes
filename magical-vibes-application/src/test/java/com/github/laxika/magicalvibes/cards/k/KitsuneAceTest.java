package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KitsuneAce.class, DuskLegionDreadnought.class, GrizzlyBears.class})
class KitsuneAceTest extends BaseCardTest {

    @Test
    void vehicleGainsFirstStrikeWhenItAttacks() {
        addReady(player1, new KitsuneAce());
        Permanent vehicle = addReady(player1, new DuskLegionDreadnought());
        crewVehicle(vehicle);

        declareAttackers(player1, List.of(indexOf(player1, vehicle)));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "That Vehicle gains first strike until end of turn");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void untapModeUntapsKitsuneAce() {
        Permanent ace = addReady(player1, new KitsuneAce());
        Permanent vehicle = addReady(player1, new DuskLegionDreadnought());
        crewVehicle(vehicle);

        assertThat(ace.isTapped()).isTrue();
        declareAttackers(player1, List.of(indexOf(player1, vehicle)));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Untap this creature");
        harness.passBothPriorities();

        assertThat(ace.isTapped()).isFalse();
    }

    @Test
    void doesNotTriggerForAnotherCreatureAttacking() {
        addReady(player1, new KitsuneAce());
        Permanent bears = addReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(indexOf(player1, bears)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    private void crewVehicle(Permanent vehicle) {
        harness.activateAbility(player1, indexOf(player1, vehicle), null, null);
        harness.passBothPriorities();
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
