package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FragmentReality.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        LlanowarElves.class, Millstone.class})
class FragmentRealityTest extends BaseCardTest {

    @Test
    void exilesCreatureAndPutsRandomLowerManaValueCreatureOntoBattlefieldTapped() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest()));
        giveFragmentReality();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getName)
                .contains("Hill Giant");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears")
                        && permanent.isTapped());
    }

    @Test
    void exilesArtifactAndUsesItsControllersLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.setLibrary(player2, List.of(new LlanowarElves(), new Forest()));
        giveFragmentReality();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getName)
                .contains("Millstone");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Llanowar Elves")
                        && permanent.isTapped());
    }

    @Test
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        giveFragmentReality();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, creature, or enchantment");
    }

    private void giveFragmentReality() {
        harness.setHand(player1, List.of(new FragmentReality()));
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
