package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.d.Duress;
import com.github.laxika.magicalvibes.cards.s.Skyscanner;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiseAgain.class, AlpineWatchdog.class, Duress.class, Skyscanner.class})
class RiseAgainTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature card from your graveyard to the battlefield")
    void returnsCreatureFromGraveyardToBattlefield() {
        Card creature = new AlpineWatchdog();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new RiseAgain()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot target non-creature card in graveyard")
    void cannotTargetNonCreatureCard() {
        Card nonCreature = new Duress();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setHand(player1, List.of(new RiseAgain()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target card in opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new AlpineWatchdog();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new RiseAgain()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Fizzles if target creature leaves graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card creature = new AlpineWatchdog();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new RiseAgain()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, creature.getId());
        harness.getGameData().playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creature.getId()));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Returns only the targeted creature and leaves other graveyard cards alone")
    void returnsOnlyTargetedCreature() {
        Card otherCreature = new AlpineWatchdog();
        Card target = new AlpineWatchdog();
        Card nonCreature = new Duress();
        harness.setGraveyard(player1, List.of(otherCreature, target, nonCreature));
        harness.setHand(player1, List.of(new RiseAgain()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard().getId()).isEqualTo(target.getId());
            assertThat(permanent.isTapped()).isFalse();
            assertThat(permanent.isSummoningSick()).isTrue();
        });
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherCreature, nonCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Rise Again");
        harness.assertNotOnBattlefield(player2, "Alpine Watchdog");
    }

    @Test
    @DisplayName("Cannot cast without selecting a target even when a creature is available")
    void cannotCastWithoutTarget() {
        harness.setGraveyard(player1, List.of(new AlpineWatchdog()));
        harness.setHand(player1, List.of(new RiseAgain()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returning an artifact creature triggers its enters-the-battlefield ability")
    void returningArtifactCreatureTriggersEnterAbility() {
        Card target = new Skyscanner();
        Card drawnCard = new Duress();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new RiseAgain()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player1, "Skyscanner");
        harness.assertNotInGraveyard(player1, "Skyscanner");
        assertThat(harness.getGameData().playerHands.get(player1.getId())).doesNotContain(drawnCard);
        harness.passBothPriorities();
        assertThat(harness.getGameData().playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
