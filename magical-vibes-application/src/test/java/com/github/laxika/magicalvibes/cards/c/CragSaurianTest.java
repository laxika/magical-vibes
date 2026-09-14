package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.ShockTroops;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CragSaurian.class, CaveIn.class, ShockTroops.class, CateranBrute.class})
class CragSaurianTest extends BaseCardTest {

    @Test
    @DisplayName("spell damage makes its controller gain control after the trigger resolves")
    void spellDamageChangesControl() {
        harness.addToBattlefield(player2, new CragSaurian());
        harness.setHand(player1, List.of(new CaveIn()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID cragSaurianId = harness.getPermanentId(player2, "Crag Saurian");
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Crag Saurian");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Crag Saurian"));
    }

    @Test
    @DisplayName("damage from a creature ability makes its controller gain control after the trigger resolves")
    void creatureAbilityDamageChangesControl() {
        harness.addToBattlefield(player2, new CragSaurian());
        addCreatureReady(player1, new ShockTroops());

        UUID cragSaurianId = harness.getPermanentId(player2, "Crag Saurian");
        harness.activateAbility(player1, 0, null, cragSaurianId);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Crag Saurian");
        harness.assertInGraveyard(player1, "Shock Troops");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Crag Saurian"));
    }

    @Test
    @DisplayName("combat damage makes the source's controller gain control after the trigger resolves")
    void combatDamageChangesControl() {
        addCreatureReady(player1, new CateranBrute());
        addCreatureReady(player2, new CragSaurian());

        declareAttackers(List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        harness.assertOnBattlefield(player1, "Crag Saurian");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Crag Saurian"));
    }
}
