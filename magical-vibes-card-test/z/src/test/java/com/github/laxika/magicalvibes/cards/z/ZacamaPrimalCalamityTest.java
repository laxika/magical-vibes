package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GatherSpecimens;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZacamaPrimalCalamity.class, BeaconOfUnrest.class, Forest.class,
        GrizzlyBears.class, LeoninScimitar.class, GloriousAnthem.class})
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

        ZacamaPrimalCalamity target = new ZacamaPrimalCalamity();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(gd.stack).isEmpty();
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

        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("The green ability destroys an enchantment")
    void greenAbilityDestroysEnchantment() {
        addReadyZacama();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, enchantment.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("The red ability deals exactly three damage and can target Zacama itself")
    void redAbilityCanDamageItself() {
        Permanent zacama = addReadyZacama();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, zacama.getId());
        harness.passBothPriorities();

        assertThat(zacama.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Zacama, Primal Calamity");
    }

    @Test
    @DisplayName("Zacama can repeatedly activate while tapped and summoning sick")
    void abilitiesDoNotRequireUntappedOrReadySource() {
        Permanent zacama = harness.addToBattlefieldAndReturn(player1, new ZacamaPrimalCalamity());
        zacama.setSummoningSick(true);
        zacama.tap();
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        assertThat(zacama.isTapped()).isTrue();
    }

    @Test
    @CardUsed(GatherSpecimens.class)
    @DisplayName("Zacama does not untap lands if its entering controller did not cast it")
    void gatheredZacamaDoesNotUntapLands() {
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        ownForest.tap();
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        opponentForest.tap();
        harness.setHand(player1, List.of(new ZacamaPrimalCalamity()));
        harness.setHand(player2, List.of(new GatherSpecimens()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Zacama, Primal Calamity");
        assertThat(gd.stack).isEmpty();
        assertThat(ownForest.isTapped()).isTrue();
        assertThat(opponentForest.isTapped()).isTrue();
    }

    private Permanent addReadyZacama() {
        Permanent zacama = harness.addToBattlefieldAndReturn(player1, new ZacamaPrimalCalamity());
        zacama.setSummoningSick(false);
        return zacama;
    }
}
