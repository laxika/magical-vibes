package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.cards.t.TrustedPegasus;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForcedLanding.class, TrustedPegasus.class, PrimordialWurm.class})
class ForcedLandingTest extends BaseCardTest {

    @Test
    void putsTargetCreatureWithFlyingOnBottomOfItsOwnersLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TrustedPegasus());
        Card targetCard = target.getCard();
        harness.setHand(player1, List.of(new ForcedLanding()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(deckSizeBefore + 1)
                .last()
                .isSameAs(targetCard);
        harness.assertNotOnBattlefield(player2, "Trusted Pegasus");
        harness.assertNotInGraveyard(player2, "Trusted Pegasus");
    }

    @Test
    void cannotTargetCreatureWithoutFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        harness.setHand(player1, List.of(new ForcedLanding()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    void putsStolenCreatureInOwnersLibraryRatherThanControllersLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TrustedPegasus());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        Card targetCard = target.getCard();
        List<Card> controllersLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));
        List<Card> expectedOwnersLibrary = new ArrayList<>(gd.playerDecks.get(player2.getId()));
        expectedOwnersLibrary.add(targetCard);
        harness.setHand(player1, List.of(new ForcedLanding()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(controllersLibrary);
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactlyElementsOf(expectedOwnersLibrary);
        harness.assertNotOnBattlefield(player1, "Trusted Pegasus");
        harness.assertNotInGraveyard(player2, "Trusted Pegasus");
    }

    @Test
    void doesNotMoveTargetThatLosesFlyingBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TrustedPegasus());
        List<Card> libraryBefore = List.copyOf(gd.playerDecks.get(player2.getId()));
        harness.setHand(player1, List.of(new ForcedLanding()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());

        target.getRemovedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Trusted Pegasus");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(libraryBefore);
        harness.assertInGraveyard(player1, "Forced Landing");
    }
}
