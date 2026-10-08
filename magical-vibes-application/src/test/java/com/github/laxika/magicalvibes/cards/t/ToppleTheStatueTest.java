package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GratefulApparition;
import com.github.laxika.magicalvibes.cards.g.GuildGlobe;
import com.github.laxika.magicalvibes.cards.i.IronBully;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ToppleTheStatue.class, GuildGlobe.class, GratefulApparition.class, IronBully.class, Plains.class})
class ToppleTheStatueTest extends BaseCardTest {

    @Test
    @DisplayName("Taps and destroys a target artifact, then draws a card")
    void tapsDestroysArtifactAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GuildGlobe());
        harness.setLibrary(player1, List.of(new GratefulApparition()));
        castAt(target.getId());

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Taps but does not destroy a nonartifact permanent, then draws a card")
    void tapsNonartifactAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GratefulApparition());
        harness.setLibrary(player1, List.of(new GuildGlobe()));
        castAt(target.getId());

        assertThat(target.isTapped()).isTrue();
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .contains(target);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Destroys an already tapped artifact and still draws")
    void destroysTappedArtifactAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GuildGlobe());
        target.tap();
        harness.setLibrary(player1, List.of(new GratefulApparition()));

        castAt(target.getId());

        harness.assertNotOnBattlefield(player2, "Guild Globe");
        harness.assertInGraveyard(player2, "Guild Globe");
        harness.assertInHand(player1, "Grateful Apparition");
    }

    @Test
    @DisplayName("Destroys an artifact creature and draws")
    void destroysArtifactCreatureAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IronBully());
        harness.setLibrary(player1, List.of(new GratefulApparition()));

        castAt(target.getId());

        harness.assertNotOnBattlefield(player2, "Iron Bully");
        harness.assertInGraveyard(player2, "Iron Bully");
        harness.assertInHand(player1, "Grateful Apparition");
    }

    @Test
    @DisplayName("Can tap its controller's land and draw without destroying it")
    void tapsOwnLandAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setLibrary(player1, List.of(new GratefulApparition()));

        castAt(target.getId());

        assertThat(target.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Plains");
        harness.assertInHand(player1, "Grateful Apparition");
    }

    @Test
    @DisplayName("Does not draw when its only target leaves before resolution")
    void doesNotDrawWithMissingTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GuildGlobe());
        harness.setLibrary(player1, List.of(new GratefulApparition()));
        prepareSpell();
        harness.castInstant(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Topple the Statue");
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new ToppleTheStatue()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void castAt(UUID targetId) {
        prepareSpell();
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
