package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RealityShift.class, GrizzlyBears.class, LlanowarElves.class, Mountain.class})
class RealityShiftTest extends BaseCardTest {

    @Test
    void exilesTargetCreatureAndItsControllerManifestsTheirTopCard() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RealityShift()));
        harness.setLibrary(player2, List.of(new LlanowarElves()));
        addRealityShiftMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.isManifested() && permanent.isFaceDown());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void manifestsNothingForTargetControllerWithEmptyLibrary() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RealityShift()));
        harness.setLibrary(player2, List.of());
        addRealityShiftMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(Permanent::isManifested);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new RealityShift()));
        addRealityShiftMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void doesNotManifestWhenTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        LlanowarElves topCard = new LlanowarElves();
        harness.setHand(player1, List.of(new RealityShift()));
        harness.setLibrary(player2, List.of(topCard));
        addRealityShiftMana();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof RealityShift);
    }

    @Test
    void canExileOwnCreatureAndManifestNoncreatureTopCard() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Mountain topCard = new Mountain();
        LlanowarElves nextCard = new LlanowarElves();
        harness.setHand(player1, List.of(new RealityShift()));
        harness.setLibrary(player1, List.of(topCard, nextCard));
        addRealityShiftMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.getCard()).isSameAs(topCard);
        assertThat(manifested.isManifested()).isTrue();
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(manifested.isFaceDown()).isTrue();
    }

    @Test
    void manifestedCreatureCanTurnFaceUpForItsManaCost() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        LlanowarElves topCard = new LlanowarElves();
        harness.setHand(player1, List.of(new RealityShift()));
        harness.setLibrary(player1, List.of(topCard));
        addRealityShiftMana();

        harness.castAndResolveInstant(player1, 0, target.getId());
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, 0);

        assertThat(manifested.getCard()).isSameAs(topCard);
        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.isManifested()).isFalse();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(1);
    }

    private void addRealityShiftMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
