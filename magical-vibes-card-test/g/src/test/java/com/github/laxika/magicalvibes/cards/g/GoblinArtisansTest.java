package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinArtisans.class, Ornithopter.class})
class GoblinArtisansTest extends BaseCardTest {

    @Test
    @DisplayName("Flips a coin and draws or counters the targeted artifact spell")
    void flipsForDrawOrCounter() {
        addCreatureReady(player1, new GoblinArtisans());
        Ornithopter ornithopter = new Ornithopter();
        harness.setHand(player1, List.of(ornithopter));
        harness.setLibrary(player1, List.of(new Ornithopter()));

        harness.castCreature(player1, 0);
        harness.activateAbility(player1, 0, null, ornithopter.getId());
        harness.passBothPriorities();

        assertThat(gameLogContains("coin flip for Goblin Artisans")).isTrue();
        if (gameLogContains("wins the coin flip for Goblin Artisans")) {
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(ornithopter.getId()));
        } else {
            harness.assertInGraveyard(player1, "Ornithopter");
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }
    }

    @Test
    @DisplayName("Cannot target an artifact spell already targeted by another Goblin Artisans")
    void cannotTargetArtifactSpellAlreadyTargetedByAnotherArtisan() {
        addCreatureReady(player1, new GoblinArtisans());
        addCreatureReady(player1, new GoblinArtisans());
        Ornithopter ornithopter = new Ornithopter();
        harness.setHand(player1, List.of(ornithopter));

        harness.castCreature(player1, 0);
        harness.activateAbility(player1, 0, null, ornithopter.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, ornithopter.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact spell");
    }

    @Test
    void cannotActivateWithoutAnArtifactSpellTarget() {
        Permanent artisan = addCreatureReady(player1, new GoblinArtisans());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(artisan.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("coin flip for Goblin Artisans")).isFalse();
    }

    @Test
    void cannotTargetAnOpponentsArtifactSpell() {
        addCreatureReady(player1, new GoblinArtisans());
        Ornithopter ornithopter = new Ornithopter();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(ornithopter));
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ornithopter.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact spell");
    }

    @Test
    void cannotTargetANonartifactCreatureSpell() {
        addCreatureReady(player1, new GoblinArtisans());
        GoblinArtisans spell = new GoblinArtisans();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void theSameArtisanCanTargetTheSpellAgainAfterUntapping() {
        Permanent artisan = addCreatureReady(player1, new GoblinArtisans());
        Ornithopter ornithopter = new Ornithopter();
        harness.setHand(player1, List.of(ornithopter));
        harness.castCreature(player1, 0);
        harness.activateAbility(player1, 0, null, ornithopter.getId());
        artisan.untap();

        harness.activateAbility(player1, 0, null, ornithopter.getId());

        assertThat(gd.stack).hasSize(3);
        assertThat(artisan.isTapped()).isTrue();
        assertThat(gameLogContains("coin flip for Goblin Artisans")).isFalse();
    }

    @Test
    void abilityStillResolvesAfterArtisanLeavesTheBattlefield() {
        Permanent artisan = addCreatureReady(player1, new GoblinArtisans());
        Ornithopter ornithopter = new Ornithopter();
        harness.setHand(player1, List.of(ornithopter));
        harness.setLibrary(player1, List.of(new Ornithopter()));
        harness.castCreature(player1, 0);
        harness.activateAbility(player1, 0, null, ornithopter.getId());
        gd.playerBattlefields.get(player1.getId()).remove(artisan);
        harness.setGraveyard(player1, List.of(artisan.getCard()));

        harness.passBothPriorities();

        assertThat(gameLogContains("coin flip for Goblin Artisans")).isTrue();
        if (gameLogContains("wins the coin flip for Goblin Artisans")) {
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(ornithopter.getId()));
        } else {
            harness.assertInGraveyard(player1, "Ornithopter");
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }
    }
}
