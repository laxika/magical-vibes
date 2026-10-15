package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.Fling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WormholeWarp.class, GrizzlyBears.class, Forest.class, Fling.class})
class WormholeWarpTest extends BaseCardTest {

    @Test
    void exilesCreatureAndOffersItsControllerRandomNonlandSideboardCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card sideboardCreature = new GrizzlyBears();
        Forest sideboardLand = new Forest();
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(sideboardLand, sideboardCreature)));

        castWormholeWarp(target);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == sideboardCreature);
        assertThat(gd.playerSideboards.get(player2.getId())).contains(sideboardLand).doesNotContain(sideboardCreature);
    }

    @Test
    void decliningLeavesSideboardCardInSideboard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card sideboardCreature = new GrizzlyBears();
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(sideboardCreature)));

        castWormholeWarp(target);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(sideboardCreature);
    }

    @Test
    void doesNotOfferLandFromSideboard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(new Forest())));

        castWormholeWarp(target);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerSideboards.get(player2.getId())).hasSize(1);
    }

    @Test
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WormholeWarp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .hasMessageContaining("opponent");
    }

    @Test
    void revealsTheNonlandCardPubliclyBeforeTheCastDecision() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card sideboardCreature = new GrizzlyBears();
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(sideboardCreature)));

        castWormholeWarp(target);

        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveal")
                && entry.plainText().contains("Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(sideboardCreature);
    }

    @Test
    void revealsEveryCardWhenSideboardContainsOnlyLands() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest sideboardLand = new Forest();
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(sideboardLand)));

        castWormholeWarp(target);

        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveal")
                && entry.plainText().contains("Forest"));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(sideboardLand);
    }

    @Test
    void exilesCreatureWhenControllerHasNoSideboard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerSideboards.remove(player2.getId());

        castWormholeWarp(target);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotOfferSideboardCardWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card sideboardCreature = new GrizzlyBears();
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(sideboardCreature)));
        harness.setHand(player1, List.of(new WormholeWarp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(sideboardCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotCastSideboardSpellWithUnpayableAdditionalCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Fling sideboardSpell = new Fling();
        sideboardSpell.setOwnerId(player2.getId());
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(sideboardSpell)));

        castWormholeWarp(target);

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player2, true);
        }

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(sideboardSpell);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == sideboardSpell);
    }

    private void castWormholeWarp(Permanent target) {
        harness.setHand(player1, List.of(new WormholeWarp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
