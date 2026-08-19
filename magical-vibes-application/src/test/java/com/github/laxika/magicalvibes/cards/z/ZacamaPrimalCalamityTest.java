package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZacamaPrimalCalamityTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, untaps all lands its controller controls")
    void castEtbUntapsControlledLandsOnly() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        opponentForest.tap();

        harness.setHand(player1, List.of(new ZacamaPrimalCalamity()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        assertThat(opponentForest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("When put onto the battlefield without being cast, Zacama does not untap lands")
    void nonCastEtbDoesNotUntapLands() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        harness.setGraveyard(player1, List.of(new ZacamaPrimalCalamity()));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Zacama, Primal Calamity");
    }

    @Test
    @DisplayName("The red ability deals 3 damage to target creature")
    void redAbilityDealsDamageToCreature() {
        addReadyZacama();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The green ability destroys an artifact or enchantment")
    void greenAbilityDestroysArtifactOrEnchantment() {
        addReadyZacama();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("The green ability cannot target a creature")
    void greenAbilityCannotTargetCreature() {
        addReadyZacama();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The white ability gains 3 life")
    void whiteAbilityGainsLife() {
        addReadyZacama();
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    private Permanent addReadyZacama() {
        Permanent zacama = new Permanent(new ZacamaPrimalCalamity());
        zacama.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(zacama);
        return zacama;
    }
}
