package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.FranticFirebolt;
import com.github.laxika.magicalvibes.cards.m.Mintstrosity;
import com.github.laxika.magicalvibes.cards.w.WitchsMark;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnrulyCatapult.class, Mintstrosity.class, WitchsMark.class, FranticFirebolt.class})
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

        harness.setHand(player1, List.of(new FranticFirebolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, catapult.getId());
        harness.passBothPriorities();

        assertThat(catapult.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a sorcery untaps Unruly Catapult")
    void sorcerySpellUntapsCatapult() {
        Permanent catapult = addCatapultReady(player1);
        catapult.tap();

        harness.setHand(player1, List.of(new WitchsMark()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(catapult.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a creature does not untap Unruly Catapult")
    void creatureSpellDoesNotUntapCatapult() {
        Permanent catapult = addCatapultReady(player1);
        catapult.tap();

        harness.setHand(player1, List.of(new Mintstrosity()));
        harness.addMana(player1, ManaColor.BLACK, 2);
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
        harness.setHand(player2, List.of(new FranticFirebolt()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, catapult.getId());

        assertThat(catapult.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The untap trigger resolves before the spell and allows another activation")
    void untapTriggerAllowsAnotherActivationBeforeSpellResolves() {
        Permanent catapult = addCatapultReady(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);

        harness.setHand(player1, List.of(new FranticFirebolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, catapult.getId());

        assertThat(catapult.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(catapult.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(catapult.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Summoning sickness prevents activating the tap ability")
    void summoningSicknessPreventsActivation() {
        Permanent catapult = addCatapultReady(player1);
        catapult.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(catapult.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A tapped Catapult cannot activate without untapping")
    void tappedCatapultCannotActivateAgain() {
        Permanent catapult = addCatapultReady(player1);
        catapult.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each Catapult untaps independently and the opponent's Catapult stays tapped")
    void spellUntapsOnlyControlledCatapults() {
        Permanent first = addCatapultReady(player1);
        Permanent second = addCatapultReady(player1);
        Permanent opposing = addCatapultReady(player2);
        first.tap();
        second.tap();
        opposing.tap();

        harness.setHand(player1, List.of(new FranticFirebolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, opposing.getId());
        assertThat(gd.stack).hasSize(3);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(opposing.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addCatapultReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new UnrulyCatapult());
        perm.setSummoningSick(false);
        return perm;
    }
}
