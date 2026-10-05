package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BesottedKnight;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HopefulVigil;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KellansLightblades.class, DarksteelRelic.class, GrizzlyBears.class, HillGiant.class,
        BesottedKnight.class, HopefulVigil.class, PropheticPrism.class})
class KellansLightbladesTest extends BaseCardTest {

    @Test
    void withoutBargainDealsThreeDamageToAnAttackingCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());
        target.setToughnessModifier(2);
        declareAttackers(List.of(0));

        castLightblades(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    void withoutBargainDealsThreeDamageToABlockingCreature() {
        addCreatureReady(player1, new HillGiant());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setToughnessModifier(2);
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        castLightblades(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void bargainDestroysTheAttackingCreatureInsteadOfDealingDamage() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());
        target.setToughnessModifier(2);
        declareAttackers(List.of(1));
        harness.setHand(player1, List.of(new KellansLightblades()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Darksteel Relic");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotTargetACreatureThatIsNotAttackingOrBlocking() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KellansLightblades()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unbargainedDamageKillsAnAttackingCreatureWithThreeToughness() {
        Permanent target = addCreatureReady(player1, new BesottedKnight());
        addCreatureReady(player2, new BesottedKnight());
        declareAttackers(List.of(0));

        castLightblades(target.getId());

        harness.assertNotOnBattlefield(player1, "Besotted Knight");
        harness.assertInGraveyard(player1, "Besotted Knight");
    }

    @Test
    void bargainDestroysABlockingCreatureWithMoreThanThreeToughness() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        addCreatureReady(player1, new BesottedKnight());
        Permanent target = addCreatureReady(player2, new BesottedKnight());
        target.setToughnessModifier(2);
        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        prepareLightblades();

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Besotted Knight");
        harness.assertInGraveyard(player1, "Prophetic Prism");
    }

    @Test
    void bargainAcceptsACreatureTokenThatIsNeitherArtifactNorEnchantment() {
        harness.enterBattlefieldAndReturn(player1, new HopefulVigil());
        resolveAllTriggers();
        Permanent sacrifice = findPermanent(player1, "Knight");
        Permanent target = addCreatureReady(player1, new BesottedKnight());
        addCreatureReady(player2, new BesottedKnight());
        declareAttackers(List.of(2));
        prepareLightblades();

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        harness.assertOnBattlefield(player1, "Hopeful Vigil");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Besotted Knight");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void bargainAcceptsANontokenEnchantment() {
        harness.setLibrary(player1, List.of());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HopefulVigil());
        Permanent target = addCreatureReady(player1, new BesottedKnight());
        addCreatureReady(player2, new BesottedKnight());
        declareAttackers(List.of(1));
        prepareLightblades();

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        harness.assertInGraveyard(player1, "Hopeful Vigil");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Besotted Knight");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void bargainCannotSacrificeANontokenCreatureWithoutAnEligibleType() {
        Permanent target = addCreatureReady(player1, new BesottedKnight());
        Permanent sacrifice = addCreatureReady(player1, new BesottedKnight());
        addCreatureReady(player2, new BesottedKnight());
        declareAttackers(List.of(0));
        prepareLightblades();

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target, sacrifice);
        harness.assertInHand(player1, "Kellan's Lightblades");
    }

    @Test
    void bargainCannotSacrificeAnOpponentsArtifact() {
        Permanent target = addCreatureReady(player1, new BesottedKnight());
        addCreatureReady(player2, new BesottedKnight());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        declareAttackers(List.of(0));
        prepareLightblades();

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(sacrifice);
        harness.assertInHand(player1, "Kellan's Lightblades");
    }

    @Test
    void unbargainedSpellDoesNothingIfTargetLeavesCombatBeforeResolution() {
        Permanent target = addCreatureReady(player1, new BesottedKnight());
        addCreatureReady(player2, new BesottedKnight());
        declareAttackers(List.of(0));
        prepareLightblades();
        harness.castInstant(player1, 0, target.getId());

        target.setAttacking(false);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Besotted Knight");
        harness.assertInGraveyard(player1, "Kellan's Lightblades");
    }

    @Test
    void bargainedSpellDoesNothingIfTargetLeavesCombatBeforeResolution() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        Permanent target = addCreatureReady(player1, new BesottedKnight());
        addCreatureReady(player2, new BesottedKnight());
        declareAttackers(List.of(1));
        prepareLightblades();
        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        target.setAttacking(false);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Besotted Knight");
        harness.assertInGraveyard(player1, "Prophetic Prism");
        harness.assertInGraveyard(player1, "Kellan's Lightblades");
    }

    private void prepareLightblades() {
        harness.setHand(player1, List.of(new KellansLightblades()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castLightblades(UUID targetId) {
        prepareLightblades();
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
