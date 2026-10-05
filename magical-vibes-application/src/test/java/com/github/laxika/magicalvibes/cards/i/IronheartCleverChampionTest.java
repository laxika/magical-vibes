package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WurmsTooth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IronheartCleverChampion.class, WurmsTooth.class, GrizzlyBears.class, Cancel.class})
class IronheartCleverChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Noncreature spells can use improvise")
    void grantsImproviseToNoncreatureSpells() {
        harness.addToBattlefield(player1, new IronheartCleverChampion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WurmsTooth());
        harness.setHand(player1, List.of(new WurmsTooth()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId()));

        assertThat(artifact.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(
                permanent -> permanent.getCard() instanceof WurmsTooth).hasSize(2);
    }

    @Test
    @DisplayName("Creature spells do not get improvise")
    void doesNotGrantImproviseToCreatureSpells() {
        harness.addToBattlefield(player1, new IronheartCleverChampion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WurmsTooth());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    void ironheartCanUseItsOwnImproviseWithoutAnotherIronheart() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WurmsTooth());
        harness.setHand(player1, List.of(new IronheartCleverChampion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Ironheart, Clever Champion");
    }

    @Test
    void ironheartCanImproviseWhileSummoningSick() {
        Permanent ironheart = harness.addToBattlefieldAndReturn(player1, new IronheartCleverChampion());
        ironheart.setSummoningSick(true);
        harness.setHand(player1, List.of(new WurmsTooth()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(ironheart.getId()));
        harness.passBothPriorities();

        assertThat(ironheart.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Wurm's Tooth");
    }

    @Test
    void doesNotGrantImproviseToOpponentsSpells() {
        harness.addToBattlefield(player2, new IronheartCleverChampion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WurmsTooth());
        harness.setHand(player1, List.of(new WurmsTooth()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    void grantsImproviseToNonartifactInstants() {
        Permanent ironheart = harness.addToBattlefieldAndReturn(player1, new IronheartCleverChampion());
        WurmsTooth target = new WurmsTooth();
        harness.setHand(player1, List.of(target, new Cancel()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()), List.of(ironheart.getId()));
        harness.passBothPriorities();

        assertThat(ironheart.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Wurm's Tooth");
        harness.assertNotOnBattlefield(player1, "Wurm's Tooth");
    }

    @Test
    void improviseCannotPayIronheartsBlueManaCost() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WurmsTooth());
        harness.setHand(player1, List.of(new IronheartCleverChampion()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    void tappedArtifactsCannotPayForImprovise() {
        harness.addToBattlefield(player1, new IronheartCleverChampion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WurmsTooth());
        artifact.tap();
        harness.setHand(player1, List.of(new WurmsTooth()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
