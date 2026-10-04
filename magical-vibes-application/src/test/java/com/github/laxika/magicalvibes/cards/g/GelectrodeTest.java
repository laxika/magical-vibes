package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Pyromatics;
import com.github.laxika.magicalvibes.cards.t.TrainOfThought;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gelectrode.class, Pyromatics.class, TrainOfThought.class})
class GelectrodeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Gelectrode deals 1 damage to a target player")
    void tappingDealsDamageToPlayer() {
        Permanent gelectrode = addCreatureReady(player1, new Gelectrode());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gelectrode.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping Gelectrode deals 1 damage to a target creature")
    void tappingDealsDamageToCreature() {
        Permanent gelectrode = addCreatureReady(player1, new Gelectrode());
        Permanent target = addCreatureReady(player2, new Gelectrode());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gelectrode.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Casting an instant may untap Gelectrode")
    void instantSpellMayUntap() {
        Permanent gelectrode = addTappedGelectrode();
        harness.setHand(player1, List.of(new Pyromatics()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gelectrode.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a sorcery may untap Gelectrode")
    void sorcerySpellMayUntap() {
        Permanent gelectrode = addTappedGelectrode();
        harness.setLibrary(player1, List.of(new Gelectrode()));
        harness.castFromHand(player1, new TrainOfThought(), "{1}{U}");
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gelectrode.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the trigger leaves Gelectrode tapped")
    void decliningUntapLeavesItTapped() {
        Permanent gelectrode = addTappedGelectrode();
        harness.setHand(player1, List.of(new Pyromatics()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gelectrode.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's instant does not trigger Gelectrode")
    void opponentSpellDoesNotTrigger() {
        Permanent gelectrode = addTappedGelectrode();
        harness.setHand(player2, List.of(new Pyromatics()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gelectrode.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a non-instant, non-sorcery spell does not trigger Gelectrode")
    void creatureSpellDoesNotTrigger() {
        Permanent gelectrode = addTappedGelectrode();
        harness.castFromHand(player1, new Gelectrode(), "{1}{U}{R}");
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gelectrode.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The tap ability cannot be activated while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new Gelectrode());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The tap ability cannot be activated while already tapped")
    void cannotActivateWhileTapped() {
        addTappedGelectrode();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Damage still resolves after Gelectrode dies in response")
    void damageResolvesAfterSourceDies() {
        Permanent gelectrode = addCreatureReady(player1, new Gelectrode());
        addCreatureReady(player2, new Gelectrode());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player2, 0, null, gelectrode.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(gelectrode.getCard());

        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("An accepted untap trigger allows a second damage activation")
    void canActivateAgainAfterUntap() {
        Permanent gelectrode = addCreatureReady(player1, new Gelectrode());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Pyromatics()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gelectrode.isTapped()).isFalse();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gelectrode.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting while untapped still triggers an untap that can resolve after tapping")
    void untappedGelectrodeStillTriggers() {
        Permanent gelectrode = addCreatureReady(player1, new Gelectrode());
        harness.setHand(player1, List.of(new Pyromatics()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gelectrode.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);

        harness.passBothPriorities();

        assertThat(gelectrode.isTapped()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);

        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    private Permanent addTappedGelectrode() {
        Permanent gelectrode = addCreatureReady(player1, new Gelectrode());
        gelectrode.tap();
        return gelectrode;
    }
}
