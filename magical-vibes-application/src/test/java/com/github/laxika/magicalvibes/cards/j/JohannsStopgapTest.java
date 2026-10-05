package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.h.HopefulVigil;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.t.ThreeBlindMice;
import com.github.laxika.magicalvibes.cards.u.UnassumingSage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JohannsStopgap.class, PropheticPrism.class, UnassumingSage.class, Island.class,
        ThreeBlindMice.class, HopefulVigil.class})
class JohannsStopgapTest extends BaseCardTest {

    @Test
    void returnsNonlandPermanentAndDrawsWithoutBargain() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnassumingSage());
        harness.setHand(player1, List.of(new JohannsStopgap()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertInHand(player2, "Unassuming Sage");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void bargainReducesCostSacrificesArtifactAndDraws() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnassumingSage());
        harness.setHand(player1, List.of(new JohannsStopgap()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Unassuming Sage");
        harness.assertInGraveyard(player1, "Prophetic Prism");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new JohannsStopgap()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = target.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    void canBargainBySacrificingAnEnchantment() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ThreeBlindMice());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnassumingSage());
        harness.setHand(player1, List.of(new JohannsStopgap()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, target.getId(), sacrifice.getId());
        harness.assertNotOnBattlefield(player1, "Three Blind Mice");
        harness.assertInGraveyard(player1, "Three Blind Mice");
        harness.passBothPriorities();

        harness.assertInHand(player2, "Unassuming Sage");
        harness.assertInHand(player1, "Island");
    }

    @Test
    void canBargainBySacrificingANonartifactNonenchantmentCreatureToken() {
        harness.enterBattlefieldAndReturn(player1, new HopefulVigil());
        resolveAllTriggers();
        Permanent sacrifice = findPermanent(player1, "Knight");
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnassumingSage());
        harness.setHand(player1, List.of(new JohannsStopgap()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Knight");
        harness.assertOnBattlefield(player1, "Hopeful Vigil");
        harness.assertInHand(player2, "Unassuming Sage");
        harness.assertInHand(player1, "Island");
    }

    @Test
    void cannotBargainBySacrificingANontokenCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        harness.setHand(player1, List.of(new JohannsStopgap()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castKickedSorceryWithSacrificeNoKickerTarget(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Unassuming Sage");
        harness.assertInHand(player1, "Johann's Stopgap");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotBargainBySacrificingAnOpponentsArtifact() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnassumingSage());
        harness.setHand(player1, List.of(new JohannsStopgap()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castKickedSorceryWithSacrificeNoKickerTarget(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Prophetic Prism");
        harness.assertInHand(player1, "Johann's Stopgap");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificingTheTargetToBargainPreventsTheDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.setHand(player1, List.of(new JohannsStopgap()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, target.getId(), target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Prophetic Prism");
        harness.assertInGraveyard(player1, "Johann's Stopgap");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotPayTheReducedCostWithoutBargaining() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnassumingSage());
        harness.setHand(player1, List.of(new JohannsStopgap()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Unassuming Sage");
        harness.assertInHand(player1, "Johann's Stopgap");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsAnOpponentsControlledArtifactToItsOwnerAndDrawsForTheCaster() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        gd.stolenCreatures.put(target.getId(), player1.getId());
        harness.setHand(player1, List.of(new JohannsStopgap()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Prophetic Prism");
        harness.assertInHand(player1, "Prophetic Prism");
        harness.assertInHand(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
