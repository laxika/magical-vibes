package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AetherstreamLeopard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.p.PrizefighterConstruct;
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

@CardUsed({InspiringStatuary.class, GrizzlyBears.class, HowlingMine.class, WurmsTooth.class,
        AetherstreamLeopard.class, ImplementOfFerocity.class, PrizefighterConstruct.class})
class InspiringStatuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Nonartifact spells can use improvise")
    void grantsImproviseToNonartifactSpells() {
        harness.addToBattlefield(player1, new InspiringStatuary());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(artifact.getId()));

        assertThat(artifact.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Artifact spells do not get improvise")
    void doesNotGrantImproviseToArtifactSpells() {
        Permanent statuary = harness.addToBattlefieldAndReturn(player1, new InspiringStatuary());
        harness.setHand(player1, List.of(new WurmsTooth()));

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(statuary.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(statuary.isTapped()).isFalse();
    }

    @Test
    void canTapStatuaryItselfAndSummoningSickArtifactCreature() {
        Permanent statuary = harness.addToBattlefieldAndReturn(player1, new InspiringStatuary());
        Permanent construct = harness.addToBattlefieldAndReturn(player1, new PrizefighterConstruct());
        construct.setSummoningSick(true);
        harness.setHand(player1, List.of(new AetherstreamLeopard()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(statuary.getId(), construct.getId()));

        assertThat(statuary.isTapped()).isTrue();
        assertThat(construct.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Aetherstream Leopard");
    }

    @Test
    void tappedStatuaryStillGrantsImprovise() {
        Permanent statuary = harness.addToBattlefieldAndReturn(player1, new InspiringStatuary());
        statuary.tap();
        Permanent implement = harness.addToBattlefieldAndReturn(player1, new ImplementOfFerocity());
        Permanent construct = harness.addToBattlefieldAndReturn(player1, new PrizefighterConstruct());
        harness.setHand(player1, List.of(new AetherstreamLeopard()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(implement.getId(), construct.getId()));

        assertThat(implement.isTapped()).isTrue();
        assertThat(construct.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Aetherstream Leopard");
    }

    @Test
    void artifactsCannotPayColoredMana() {
        Permanent statuary = harness.addToBattlefieldAndReturn(player1, new InspiringStatuary());
        Permanent implement = harness.addToBattlefieldAndReturn(player1, new ImplementOfFerocity());
        Permanent construct = harness.addToBattlefieldAndReturn(player1, new PrizefighterConstruct());
        harness.setHand(player1, List.of(new AetherstreamLeopard()));

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).doesNotContain(0);
        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(statuary.getId(), implement.getId(), construct.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(statuary.isTapped()).isFalse();
        assertThat(implement.isTapped()).isFalse();
        assertThat(construct.isTapped()).isFalse();
    }

    @Test
    void opponentStatuaryDoesNotGrantImprovise() {
        harness.addToBattlefield(player2, new InspiringStatuary());
        Permanent implement = harness.addToBattlefieldAndReturn(player1, new ImplementOfFerocity());
        Permanent construct = harness.addToBattlefieldAndReturn(player1, new PrizefighterConstruct());
        harness.setHand(player1, List.of(new AetherstreamLeopard()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).doesNotContain(0);
        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(implement.getId(), construct.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(implement.isTapped()).isFalse();
        assertThat(construct.isTapped()).isFalse();
    }

    @Test
    void cannotTapNonartifactCreatureForImprovise() {
        harness.addToBattlefield(player1, new InspiringStatuary());
        Permanent leopard = harness.addToBattlefieldAndReturn(player1, new AetherstreamLeopard());
        harness.setHand(player1, List.of(new AetherstreamLeopard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(), List.of(leopard.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(leopard.isTapped()).isFalse();
    }

    @Test
    void cannotUseTappedArtifactForImprovise() {
        Permanent statuary = harness.addToBattlefieldAndReturn(player1, new InspiringStatuary());
        statuary.tap();
        harness.setHand(player1, List.of(new AetherstreamLeopard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(), List.of(statuary.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(statuary.isTapped()).isTrue();
    }
}
