package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhoenixDown.class, AirElemental.class, GrizzlyBears.class, WalkingCorpse.class})
class PhoenixDownTest extends BaseCardTest {

    @Test
    void returnsAQualifyingCreatureTappedAndExilesPhoenixDown() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addToBattlefield(player1, new PhoenixDown());
        addActivationMana();

        harness.activateAbility(player1, 0, 0, 0, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Phoenix Down"));
    }

    @Test
    void exilesASelectedZombieAndPhoenixDown() {
        harness.addToBattlefield(player1, new PhoenixDown());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        addActivationMana();

        harness.activateAbility(player1, 0, 1, null, zombie.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phoenix Down");
        harness.assertNotOnBattlefield(player1, "Walking Corpse");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Phoenix Down", "Walking Corpse");
    }

    @Test
    void cannotExileAPermanentWithoutAnUndeadSubtype() {
        harness.addToBattlefield(player1, new PhoenixDown());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Phoenix Down");
    }

    @Test
    void cannotReturnACreatureWithManaValueGreaterThanFour() {
        AirElemental creature = new AirElemental();
        harness.setGraveyard(player1, List.of(creature));
        harness.addToBattlefield(player1, new PhoenixDown());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, 0, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Phoenix Down");
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
