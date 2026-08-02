package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HorobiDeathsWailTest extends BaseCardTest {

    private boolean onBattlefield(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals(name));
    }

    private boolean inGraveyard(Player owner, String name) {
        return gd.playerGraveyards.get(owner.getId()).stream()
                .anyMatch(c -> c.getName().equals(name));
    }

    @Test
    @DisplayName("Destroys a creature targeted by an opponent's spell")
    void destroysCreatureTargetedBySpell() {
        harness.addToBattlefield(player1, new HorobiDeathsWail());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bearsId);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Horobi, Death's Wail");

        harness.passBothPriorities();

        assertThat(onBattlefield(player1, "Grizzly Bears")).isFalse();
        assertThat(inGraveyard(player1, "Grizzly Bears")).isTrue();
    }

    @Test
    @DisplayName("Triggers on the controller's own spell too")
    void triggersOnOwnSpell() {
        harness.addToBattlefield(player1, new HorobiDeathsWail());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        assertThat(onBattlefield(player1, "Grizzly Bears")).isFalse();
        assertThat(inGraveyard(player1, "Grizzly Bears")).isTrue();
    }

    @Test
    @DisplayName("Triggers when an activated ability targets a creature")
    void triggersOnActivatedAbility() {
        harness.addToBattlefield(player1, new HorobiDeathsWail());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        Permanent firecannon = new Permanent(new ElaborateFirecannon());
        firecannon.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(firecannon);

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, null, bearsId);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Horobi, Death's Wail");
    }

    @Test
    @DisplayName("Does not trigger when a spell targets a player")
    void doesNotTriggerOnPlayerTarget() {
        harness.addToBattlefield(player1, new HorobiDeathsWail());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }
}
