package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AngrathsMarauders;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NovelNunchaku.class, GrizzlyBears.class, HillGiant.class, Naturalize.class, AngrathsMarauders.class})
class NovelNunchakuTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached and its reflexive ability fights an opponent's creature")
    void entersAttachedAndFights() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        castNunchaku(giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        Permanent nunchaku = findPermanent(player1, "Novel Nunchaku");
        assertThat(nunchaku.getAttachedTo()).isEqualTo(giant.getId());
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.TRAMPLE)).isTrue();
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The reflexive fight may choose no target")
    void mayChooseNoFightTarget() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        castNunchaku(giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(giant.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The enter ability cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NovelNunchaku()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equip attaches Novel Nunchaku to a creature you control")
    void equipAttachesToCreature() {
        Permanent nunchaku = harness.addToBattlefieldAndReturn(player1, new NovelNunchaku());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, giant.getId());
        harness.passBothPriorities();

        assertThat(nunchaku.getAttachedTo()).isEqualTo(giant.getId());
    }

    @Test
    @DisplayName("The pending fight still happens after the Equipment is destroyed")
    void fightsAfterEquipmentIsDestroyed() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        castNunchaku(giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent nunchaku = findPermanent(player1, "Novel Nunchaku");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, nunchaku.getId());
        harness.assertInGraveyard(player1, "Novel Nunchaku");
        harness.passBothPriorities();

        assertThat(giant.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Each fighting creature uses its own controller's damage modifiers")
    void opponentDamageIsNotDoubledByOurMarauders() {
        harness.addToBattlefield(player1, new AngrathsMarauders());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        castNunchaku(giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroying the Equipment before attachment prevents the reflexive fight")
    void noFightWhenEquipmentCannotAttach() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        castNunchaku(giant.getId());
        harness.passBothPriorities();
        Permanent nunchaku = findPermanent(player1, "Novel Nunchaku");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, nunchaku.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Novel Nunchaku");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(giant.getMarkedDamage()).isZero();
        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The reflexive fight cannot target a creature you control")
    void cannotFightOwnCreature() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());

        castNunchaku(giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opposingBears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(ownBears.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Equipping again transfers the boost and trample without triggering another fight")
    void equipTransfersBonusesWithoutFighting() {
        Permanent nunchaku = harness.addToBattlefieldAndReturn(player1, new NovelNunchaku());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());
        nunchaku.setAttachedTo(giant.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, ownBears.getId());
        harness.passBothPriorities();

        assertThat(nunchaku.getAttachedTo()).isEqualTo(ownBears.getId());
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.TRAMPLE)).isTrue();
        assertThat(ownBears.getMarkedDamage()).isZero();
        assertThat(opposingBears.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Novel Nunchaku can be cast without a creature to attach to")
    void canCastWithoutCreatures() {
        harness.setHand(player1, List.of(new NovelNunchaku()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Novel Nunchaku").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castNunchaku(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new NovelNunchaku()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0, targetId);
    }
}
