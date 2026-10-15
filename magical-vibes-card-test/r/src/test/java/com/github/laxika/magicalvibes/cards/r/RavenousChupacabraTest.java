package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MomentOfCraving;
import com.github.laxika.magicalvibes.cards.z.ZetalpaPrimalDawn;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavenousChupacabra.class, RaptorCompanion.class, MomentOfCraving.class, ZetalpaPrimalDawn.class})
class RavenousChupacabraTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys the targeted creature an opponent controls")
    void etbDestroysTargetedOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        harness.setHand(player1, List.of(new RavenousChupacabra()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        harness.setHand(player1, List.of(new RavenousChupacabra()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.handlePermanentChosen(player1, legalTarget.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Enters successfully with no trigger left on the stack when no legal target exists")
    void entersWithoutValidTarget() {
        harness.setHand(player1, List.of(new RavenousChupacabra()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void creatureSpellDoesNotRequireAnEtbTargetAtCastTime() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        harness.setHand(player1, List.of(new RavenousChupacabra()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    void indestructibleCreatureSurvivesDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZetalpaPrimalDawn());
        harness.setHand(player1, List.of(new RavenousChupacabra()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target.getCard());
    }

    @Test
    void triggerResolvesAfterItsSourceDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        harness.setHand(player1, List.of(new RavenousChupacabra(), new MomentOfCraving()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.castAndResolveInstant(player1, 0, source.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }
}
