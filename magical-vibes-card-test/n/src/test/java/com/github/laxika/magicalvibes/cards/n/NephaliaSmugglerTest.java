package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FarbogExplorer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NephaliaSmuggler.class, FarbogExplorer.class})
class NephaliaSmugglerTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers another creature you control and taps the Smuggler")
    void flickersOwnCreature() {
        addCreatureReady(player1, new NephaliaSmuggler());
        harness.addToBattlefield(player1, new FarbogExplorer());
        harness.addMana(player1, ManaColor.BLUE, 4);

        UUID creatureId = harness.getPermanentId(player1, "Farbog Explorer");

        harness.activateAbility(player1, 0, null, creatureId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Farbog Explorer");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Farbog Explorer"));
        // Returned as a new object, so it is summoning sick again.
        assertThat(findPermanent(player1, "Farbog Explorer").isSummoningSick()).isTrue();
        assertThat(findPermanent(player1, "Nephalia Smuggler").isTapped()).isTrue();
        assertThat(harness.getPermanentId(player1, "Farbog Explorer")).isNotEqualTo(creatureId);
    }

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetItself() {
        addCreatureReady(player1, new NephaliaSmuggler());
        harness.addMana(player1, ManaColor.BLUE, 4);

        UUID smugglerId = harness.getPermanentId(player1, "Nephalia Smuggler");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, smugglerId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addCreatureReady(player1, new NephaliaSmuggler());
        harness.addToBattlefield(player2, new FarbogExplorer());
        harness.addMana(player1, ManaColor.BLUE, 4);

        UUID opponentCreatureId = harness.getPermanentId(player2, "Farbog Explorer");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetRemoved() {
        addCreatureReady(player1, new NephaliaSmuggler());
        harness.addToBattlefield(player1, new FarbogExplorer());
        harness.addMana(player1, ManaColor.BLUE, 4);

        UUID creatureId = harness.getPermanentId(player1, "Farbog Explorer");

        harness.activateAbility(player1, 0, null, creatureId);

        Permanent creature = gqs.findPermanentById(gd, creatureId);
        gd.playerBattlefields.get(player1.getId()).remove(creature);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new NephaliaSmuggler());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FarbogExplorer());
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutBlueMana() {
        addCreatureReady(player1, new NephaliaSmuggler());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FarbogExplorer());
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithInsufficientMana() {
        addCreatureReady(player1, new NephaliaSmuggler());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FarbogExplorer());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resolvesAfterSmugglerLeavesBattlefield() {
        Permanent smuggler = addCreatureReady(player1, new NephaliaSmuggler());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FarbogExplorer());
        target.tap();
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(smuggler);

        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Farbog Explorer");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.isTapped()).isFalse();
    }

    @Test
    void returnsOpponentOwnedCreatureUnderYourControl() {
        addCreatureReady(player1, new NephaliaSmuggler());
        FarbogExplorer card = new FarbogExplorer();
        card.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Farbog Explorer");
        harness.assertNotOnBattlefield(player2, "Farbog Explorer");
        assertThat(harness.getPermanentId(player1, "Farbog Explorer")).isNotEqualTo(target.getId());
    }

    @Test
    void targetBecomingOpponentControlledIsNotFlickered() {
        addCreatureReady(player1, new NephaliaSmuggler());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FarbogExplorer());
        target.tap();
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player1.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Farbog Explorer");
        assertThat(harness.getPermanentId(player2, "Farbog Explorer")).isEqualTo(target.getId());
        assertThat(target.isTapped()).isTrue();
    }
}
