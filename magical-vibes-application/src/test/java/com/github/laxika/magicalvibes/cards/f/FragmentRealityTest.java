package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.v.ValMaroonedSurveyor;
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
        LlanowarElves.class, Millstone.class, GloriousAnthem.class})
class FragmentRealityTest extends BaseCardTest {

    @Test
    void exilesCreatureAndPutsRandomLowerManaValueCreatureOntoBattlefieldTapped() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest()));
        giveFragmentReality();

        harness.castAndResolveInstant(player1, 0, target.getId());

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

        harness.castAndResolveInstant(player1, 0, target.getId());

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

    @Test
    void exilesEnchantmentAndLeavesIneligibleCardsInLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Card creature = new GrizzlyBears();
        Card artifact = new Millstone();
        Card expensiveCreature = new HillGiant();
        harness.setLibrary(player2, List.of(artifact, creature, expensiveCreature));
        giveFragmentReality();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(artifact, expensiveCreature);
    }

    @Test
    void equalManaValueCreatureIsNotEligible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Card creature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(creature));
        giveFragmentReality();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(creature);
    }

    @Test
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        giveFragmentReality();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetTokenCreature() {
        Card token = new GrizzlyBears();
        token.setToken(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, token);
        giveFragmentReality();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void missingTargetDoesNotPutCreatureOntoBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Card creature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(creature));
        giveFragmentReality();
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target.getCard());
    }

    @Test
    void faceDownTargetHasZeroManaValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.setFaceDownAsCloaked();
        Card creature = new LlanowarElves();
        harness.setLibrary(player2, List.of(creature));
        giveFragmentReality();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(creature);
    }

    @Test
    @CardUsed(ValMaroonedSurveyor.class)
    void randomCreatureSelectionDoesNotTriggerSeekAbilities() {
        harness.addToBattlefield(player2, new ValMaroonedSurveyor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());
        giveFragmentReality();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, player1Life);
        harness.assertLife(player2, player2Life);
    }

    private void giveFragmentReality() {
        harness.setHand(player1, List.of(new FragmentReality()));
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
