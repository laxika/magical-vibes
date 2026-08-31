package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnrulyCatapult.class, GrizzlyBears.class, Ponder.class, Shock.class})
class UnrulyCatapultTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability deals 1 damage to each opponent")
    void tapAbilityDealsDamage() {
        Permanent catapult = addCatapultReady(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        assertThat(catapult.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Casting an instant untaps Unruly Catapult")
    void instantSpellUntapsCatapult() {
        Permanent catapult = addCatapultReady(player1);
        catapult.tap();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(catapult.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a sorcery untaps Unruly Catapult")
    void sorcerySpellUntapsCatapult() {
        Permanent catapult = addCatapultReady(player1);
        catapult.tap();

        harness.setHand(player1, List.of(new Ponder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(catapult.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a creature does not untap Unruly Catapult")
    void creatureSpellDoesNotUntapCatapult() {
        Permanent catapult = addCatapultReady(player1);
        catapult.tap();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(catapult.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent casting an instant does not untap Unruly Catapult")
    void opponentInstantDoesNotUntapCatapult() {
        Permanent catapult = addCatapultReady(player1);
        catapult.tap();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(catapult.isTapped()).isTrue();
    }

    private Permanent addCatapultReady(Player player) {
        Permanent perm = new Permanent(new UnrulyCatapult());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
