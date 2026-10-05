package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaskedBandits.class, Island.class})
class MaskedBanditsTest extends BaseCardTest {

    @Test
    void cannotCastFromExileWithoutResolvingItsHandAbility() {
        MaskedBandits bandits = new MaskedBandits();
        harness.setExile(player1, List.of(bandits));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, bandits.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(bandits.getId())).isNotNull();
    }

    @Test
    void losingTheTargetLandPreventsTheExileCastingPermission() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        MaskedBandits bandits = new MaskedBandits();
        harness.setHand(player1, List.of(bandits));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, land.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, land));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, bandits.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(bandits.getId())).isNotNull();
    }

    @Test
    void grantedManaCanPayForCastingMaskedBandits() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        MaskedBandits bandits = new MaskedBandits();
        harness.setHand(player1, List.of(bandits));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        assertThat(land.isTapped()).isTrue();
        harness.castFromExile(player1, bandits.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Masked Bandits");
        assertThat(gd.findExiledCard(bandits.getId())).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void canGrantManaAbilityToAnOpponentsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new MaskedBandits()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "BLACK");

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void attacksWithoutTappingAndRequiresAtLeastTwoBlockers() {
        Permanent attacker = addCreatureReady(player1, new MaskedBandits());
        Permanent firstBlocker = addCreatureReady(player2, new MaskedBandits());
        Permanent secondBlocker = addCreatureReady(player2, new MaskedBandits());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(attacker.isTapped()).isFalse();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    void handAbilityExilesTheCardAndGrantsOnlyBlackRedOrGreenMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        MaskedBandits bandits = new MaskedBandits();
        harness.setHand(player1, List.of(bandits));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, land.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(bandits.getId())).isNotNull();

        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("BLACK", "RED", "GREEN");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    void landGrantEndsWhenMaskedBanditsIsCastFromExile() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        MaskedBandits bandits = new MaskedBandits();
        harness.setHand(player1, List.of(bandits));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");
        land.untap();

        harness.castFromExile(player1, bandits.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
