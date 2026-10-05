package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.e.EverflowingChalice;
import com.github.laxika.magicalvibes.cards.g.GnarlidPack;
import com.github.laxika.magicalvibes.cards.q.QuestForRenewal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NaturesClaim.class, EverflowingChalice.class, QuestForRenewal.class, GnarlidPack.class, DarksteelCitadel.class})
class NaturesClaimTest extends BaseCardTest {

    @Test
    @DisplayName("Can destroy your own artifact to gain 4 life")
    void destroysOwnArtifactAndGainsLife() {
        harness.addToBattlefield(player1, new EverflowingChalice());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new NaturesClaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Everflowing Chalice"));

        harness.assertNotOnBattlefield(player1, "Everflowing Chalice");
        harness.assertInGraveyard(player1, "Everflowing Chalice");
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An indestructible artifact survives but its controller still gains life")
    void indestructibleArtifactStillGainsLife() {
        harness.addToBattlefield(player2, new DarksteelCitadel());
        harness.setLife(player1, 20);
        harness.setLife(player2, 15);
        harness.setHand(player1, List.of(new NaturesClaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Darksteel Citadel"));

        harness.assertOnBattlefield(player2, "Darksteel Citadel");
        harness.assertNotInGraveyard(player2, "Darksteel Citadel");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A regenerated artifact survives but its controller still gains life")
    void regeneratedArtifactStillGainsLife() {
        harness.addToBattlefield(player2, new EverflowingChalice());
        harness.setLife(player2, 15);
        harness.setHand(player1, List.of(new NaturesClaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.getGameData().playerBattlefields.get(player2.getId()).getFirst().setRegenerationShield(1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Everflowing Chalice"));

        harness.assertOnBattlefield(player2, "Everflowing Chalice");
        harness.assertNotInGraveyard(player2, "Everflowing Chalice");
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()).getFirst().isTapped()).isTrue();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Destroys target artifact and its controller gains 4 life")
    void destroysArtifactAndItsControllerGainsLife() {
        harness.addToBattlefield(player2, new EverflowingChalice());
        harness.setLife(player1, 20);
        harness.setLife(player2, 15);
        harness.setHand(player1, List.of(new NaturesClaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Everflowing Chalice");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Everflowing Chalice");
        harness.assertInGraveyard(player2, "Everflowing Chalice");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Destroys target enchantment and its controller gains 4 life")
    void destroysEnchantmentAndItsControllerGainsLife() {
        harness.addToBattlefield(player2, new QuestForRenewal());
        harness.setLife(player2, 15);
        harness.setHand(player1, List.of(new NaturesClaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Quest for Renewal");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Quest for Renewal");
        harness.assertInGraveyard(player2, "Quest for Renewal");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not gain life when the target is removed before resolution")
    void fizzlesWhenTargetIsRemovedBeforeResolution() {
        harness.addToBattlefield(player2, new EverflowingChalice());
        harness.setLife(player2, 15);
        harness.setHand(player1, List.of(new NaturesClaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Everflowing Chalice");
        harness.castInstant(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Nature's Claim");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GnarlidPack());
        harness.setHand(player1, List.of(new NaturesClaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID creatureId = harness.getPermanentId(player2, "Gnarlid Pack");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
