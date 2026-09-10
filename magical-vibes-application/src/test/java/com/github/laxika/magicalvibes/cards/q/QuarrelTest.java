package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Quarrel.class, AirElemental.class, GrizzlyBears.class, LlanowarElves.class})
class QuarrelTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature deals its power as damage to an opponent creature")
    void dealsPowerDamageToOpponentCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new Quarrel()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID sourceId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(sourceId, targetId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("The source does not receive damage from Quarrel")
    void damageIsOneSided() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new Quarrel()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(source.getMarkedDamage()).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target an opponent creature as the source")
    void cannotTargetOpponentCreatureAsSource() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new Quarrel()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID sourceId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(sourceId, targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Cannot target your own creature as the victim")
    void cannotTargetOwnCreatureAsVictim() {
        GrizzlyBears source = new GrizzlyBears();
        GrizzlyBears target = new GrizzlyBears();
        harness.addToBattlefield(player1, source);
        harness.addToBattlefield(player1, target);
        harness.setHand(player1, List.of(new Quarrel()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        List<Permanent> permanents = harness.getGameData().playerBattlefields.get(player1.getId());
        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(permanents.get(0).getId(), permanents.get(1).getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }
}
