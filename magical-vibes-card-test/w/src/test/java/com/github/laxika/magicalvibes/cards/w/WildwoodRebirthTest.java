package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.Slaughterhorn;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildwoodRebirth.class, Slaughterhorn.class, Naturalize.class})
class WildwoodRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("Wildwood Rebirth returns target creature card from graveyard to hand")
    void returnsCreatureFromGraveyardToHand() {
        Card creature = new Slaughterhorn();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WildwoodRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Wildwood Rebirth cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        Card instant = new Naturalize();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new WildwoodRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wildwood Rebirth cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new Slaughterhorn();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new WildwoodRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Wildwood Rebirth returns only the targeted creature card")
    void returnsOnlyTargetedCreature() {
        Card target = new Slaughterhorn();
        Card otherCreature = new Slaughterhorn();
        Card noncreature = new Naturalize();
        harness.setGraveyard(player1, List.of(target, otherCreature, noncreature));
        harness.setHand(player1, List.of(new WildwoodRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherCreature, noncreature)
                .doesNotContain(target);
        harness.assertInGraveyard(player1, "Wildwood Rebirth");
    }

    @Test
    @DisplayName("Wildwood Rebirth requires a target even when a creature card is available")
    void cannotCastWithoutTarget() {
        harness.setGraveyard(player1, List.of(new Slaughterhorn()));
        harness.setHand(player1, List.of(new WildwoodRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wildwood Rebirth does not return another creature when its target leaves the graveyard")
    void doesNotReturnAnotherCreatureWhenTargetLeavesGraveyard() {
        Card target = new Slaughterhorn();
        Card otherCreature = new Slaughterhorn();
        harness.setGraveyard(player1, List.of(target, otherCreature));
        harness.setHand(player1, List.of(new WildwoodRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(otherCreature));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherCreature);
        harness.assertInGraveyard(player1, "Wildwood Rebirth");
        assertThat(gd.stack).isEmpty();
    }
}
