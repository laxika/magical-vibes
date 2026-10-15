package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.p.PhyrexianFleshgorger;
import com.github.laxika.magicalvibes.cards.t.TocasiasWelcome;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RepairAndRecharge.class, MindStone.class, TocasiasWelcome.class, JaceBeleren.class, GrizzlyBears.class, PhyrexianFleshgorger.class})
class RepairAndRechargeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an artifact and creates a tapped Powerstone")
    void returnsArtifactAndCreatesPowerstone() {
        Card artifact = new MindStone();
        castWithTarget(artifact);

        harness.assertOnBattlefield(player1, "Mind Stone");
        Permanent powerstone = findPermanents(player1, "Powerstone").getFirst();
        assertThat(powerstone.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(powerstone.getCard().getSubtypes()).containsExactly(CardSubtype.POWERSTONE);
        assertThat(powerstone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Returns an enchantment or planeswalker card")
    void returnsEnchantmentOrPlaneswalker() {
        Card enchantment = new TocasiasWelcome();
        castWithTarget(enchantment);
        harness.assertOnBattlefield(player1, "Tocasia's Welcome");

        Card planeswalker = new JaceBeleren();
        harness.setGraveyard(player1, List.of(planeswalker));
        harness.setHand(player1, List.of(new RepairAndRecharge()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());
        harness.assertOnBattlefield(player1, "Jace Beleren");
    }

    @Test
    @DisplayName("Rejects a creature card as a target")
    void rejectsCreatureTarget() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new RepairAndRecharge()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsArtifactInOpponentsGraveyard() {
        Card artifact = new MindStone();
        harness.setGraveyard(player2, List.of(artifact));
        harness.setHand(player1, List.of(new RepairAndRecharge()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player2, "Mind Stone");
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
    }

    @Test
    void requiresTargetEvenWhenOnlyPowerstoneIsWanted() {
        harness.setHand(player1, List.of(new RepairAndRecharge()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, (java.util.UUID) null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
    }

    @Test
    void createsNoPowerstoneWhenTargetLeavesGraveyard() {
        Card artifact = new MindStone();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new RepairAndRecharge()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0, artifact.getId());
        harness.setGraveyard(player1, List.of());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mind Stone");
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
        harness.assertInGraveyard(player1, "Repair and Recharge");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsUntappedArtifactAndCreatesExactlyOnePowerstoneForController() {
        castWithTarget(new MindStone());

        harness.assertNotInGraveyard(player1, "Mind Stone");
        assertThat(findPermanent(player1, "Mind Stone").isTapped()).isFalse();
        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
        assertThat(findPermanents(player2, "Powerstone")).isEmpty();
        harness.assertInGraveyard(player1, "Repair and Recharge");
    }

    @Test
    void powerstoneProducesRestrictedManaAfterUntapping() {
        castWithTarget(new TocasiasWelcome());
        Permanent powerstone = findPermanent(player1, "Powerstone");
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(powerstone);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.performUntapStep(player1);
        harness.activateAbility(player1, index, null, null);

        assertThat(powerstone.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        harness.setHand(player1, List.of(new TocasiasWelcome()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, (java.util.UUID) null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Tocasia's Welcome");
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
    }

    @Test
    void returnsArtifactCreatureDespiteCreatureOnlyTargetsBeingIllegal() {
        castWithTarget(new PhyrexianFleshgorger());

        harness.assertOnBattlefield(player1, "Phyrexian Fleshgorger");
        harness.assertNotInGraveyard(player1, "Phyrexian Fleshgorger");
        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
    }

    @Test
    void powerstoneManaCanPayForArtifactSpell() {
        castWithTarget(new PhyrexianFleshgorger());
        harness.performUntapStep(player1);
        Permanent powerstone = findPermanent(player1, "Powerstone");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(powerstone), null, null);
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mind Stone")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isZero();
    }

    private void castWithTarget(Card target) {
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new RepairAndRecharge()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }
}
