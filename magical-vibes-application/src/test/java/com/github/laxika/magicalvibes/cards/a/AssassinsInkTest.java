package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BowOfNylea;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.j.JukaiPreserver;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({AssassinsInk.class, FountainOfYouth.class, GarrukWildspeaker.class, GloriousAnthem.class,
        GrizzlyBears.class, Plains.class, AutomatedArtificer.class, JukaiPreserver.class, BowOfNylea.class})
class AssassinsInkTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature")
    void destroysTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWithMana(target, 2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys a target planeswalker")
    void destroysTargetPlaneswalker() {
        Permanent target = addReadyPlaneswalker(player2);

        castWithMana(target, 2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
        harness.assertInGraveyard(player2, "Garruk Wildspeaker");
    }

    @Test
    @DisplayName("Costs one less when its controller controls an artifact")
    void costsOneLessWithArtifact() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AssassinsInk()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Costs one less when its controller controls an enchantment")
    void costsOneLessWithEnchantment() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AssassinsInk()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Costs two less when its controller controls an artifact and an enchantment")
    void costsTwoLessWithArtifactAndEnchantment() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AssassinsInk()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opponent's artifact and enchantment do not reduce the cost")
    void opponentPermanentsDoNotReduceCost() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GloriousAnthem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AssassinsInk()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Rejects a land target")
    void rejectsLandTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new AssassinsInk()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    @DisplayName("A single artifact enchantment supplies both cost reductions")
    void singleArtifactEnchantmentReducesCostTwice() {
        harness.addToBattlefield(player1, new BowOfNylea());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AutomatedArtificer());

        castWithMana(target, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Automated Artificer");
    }

    @Test
    @DisplayName("Multiple artifacts still reduce the cost by only one")
    void multipleArtifactsDoNotMultiplyReduction() {
        harness.addToBattlefield(player1, new AutomatedArtificer());
        harness.addToBattlefield(player1, new AutomatedArtificer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JukaiPreserver());
        harness.setHand(player1, List.of(new AssassinsInk()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Jukai Preserver");
    }

    @Test
    @DisplayName("An enchantment creature being targeted supplies its controller's reduction")
    void ownEnchantmentCreatureSuppliesReduction() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JukaiPreserver());

        castWithMana(target, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jukai Preserver");
    }

    @Test
    @DisplayName("Cost reductions cannot replace the required black mana")
    void reductionsDoNotReduceBlackMana() {
        harness.addToBattlefield(player1, new AutomatedArtificer());
        harness.addToBattlefield(player1, new JukaiPreserver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AutomatedArtificer());
        harness.setHand(player1, List.of(new AssassinsInk()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void castWithMana(Permanent target, int genericMana) {
        harness.setHand(player1, List.of(new AssassinsInk()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, genericMana);
        harness.castInstant(player1, 0, target.getId());
    }

    private Permanent addReadyPlaneswalker(Player player) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        planeswalker.setSummoningSick(false);
        return planeswalker;
    }
}
