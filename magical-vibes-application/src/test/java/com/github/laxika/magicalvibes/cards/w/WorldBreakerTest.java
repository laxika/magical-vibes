package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WorldBreaker.class, MindStone.class, GloriousAnthem.class, Forest.class, GrizzlyBears.class})
class WorldBreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Cast trigger exiles a target artifact")
    void castTriggerExilesArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());

        castWorldBreaker(target);

        harness.assertNotOnBattlefield(player2, "Mind Stone");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Mind Stone"));
    }

    @Test
    @DisplayName("Cast trigger exiles a target enchantment")
    void castTriggerExilesEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        castWorldBreaker(target);

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Glorious Anthem"));
    }

    @Test
    @DisplayName("Cast trigger exiles a target land")
    void castTriggerExilesLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        castWorldBreaker(target);

        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Cast trigger cannot target a creature")
    void castTriggerRejectsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        prepareWorldBreakerInHand();

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cast trigger does not trigger when there are no legal targets")
    void castTriggerDoesNotTriggerWithoutLegalTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepareWorldBreakerInHand();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "World Breaker");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Graveyard ability sacrifices a land and returns World Breaker to hand")
    void graveyardAbilityReturnsToHand() {
        harness.setGraveyard(player1, List.of(new WorldBreaker()));
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "World Breaker");
        harness.assertNotInGraveyard(player1, "World Breaker");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Graveyard ability requires a land to sacrifice")
    void graveyardAbilityRequiresLand() {
        harness.setGraveyard(player1, List.of(new WorldBreaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWorldBreaker(Permanent target) {
        prepareWorldBreakerInHand();
        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void prepareWorldBreakerInHand() {
        harness.setHand(player1, List.of(new WorldBreaker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
