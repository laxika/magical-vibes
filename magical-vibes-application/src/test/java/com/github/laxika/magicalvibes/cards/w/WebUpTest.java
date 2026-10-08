package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WebUp.class, Forest.class, GrizzlyBears.class, Naturalize.class})
class WebUpTest extends BaseCardTest {

    private void prepareToCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new WebUp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void castAndResolve(UUID targetId) {
        prepareToCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles target nonland permanent an opponent controls")
    void etbExilesTargetNonlandPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castAndResolve(targetId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(targetId));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiled permanent returns when Web Up leaves the battlefield")
    void exiledPermanentReturnsWhenSourceLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolve(targetId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID sourceId = harness.getPermanentId(player1, "Web Up");

        harness.castAndResolveInstant(player2, 0, sourceId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GrizzlyBears);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiles an opposing enchantment as well as creatures")
    void exilesOpposingEnchantment() {
        harness.addToBattlefield(player2, new WebUp());
        UUID targetId = harness.getPermanentId(player2, "Web Up");

        castAndResolve(targetId);

        harness.assertNotOnBattlefield(player2, "Web Up");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card instanceof WebUp);
        harness.assertOnBattlefield(player1, "Web Up");
    }

    @Test
    @DisplayName("Does not exile the target if Web Up leaves before its trigger resolves")
    void sourceLeavesBeforeTriggerResolves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        prepareToCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        UUID sourceId = harness.getPermanentId(player1, "Web Up");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, sourceId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Web Up");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(targetId));
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not exile a target that leaves before the trigger resolves")
    void targetLeavesBeforeTriggerResolves() {
        harness.addToBattlefield(player2, new WebUp());
        UUID targetId = harness.getPermanentId(player2, "Web Up");
        prepareToCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Web Up");
        harness.assertOnBattlefield(player1, "Web Up");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only an opponent's nonland permanent can be targeted")
    void rejectsLandAndOwnPermanentTargets() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID landId = harness.getPermanentId(player2, "Forest");
        UUID ownPermanentId = harness.getPermanentId(player1, "Grizzly Bears");

        prepareToCast();
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new WebUp()));
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownPermanentId))
                .isInstanceOf(IllegalStateException.class);
    }
}
