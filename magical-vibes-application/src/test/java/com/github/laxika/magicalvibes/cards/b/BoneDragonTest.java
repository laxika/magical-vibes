package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoneDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Graveyard ability returns Bone Dragon to the battlefield tapped, exiling seven other cards")
    void graveyardAbilityReturnsSelfTapped() {
        List<com.github.laxika.magicalvibes.model.Card> graveyard = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            graveyard.add(new GrizzlyBears());
        }
        graveyard.add(new BoneDragon());
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateGraveyardAbility(player1, 7);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bone Dragon");
        Permanent dragon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bone Dragon"))
                .findFirst().orElseThrow();
        assertThat(dragon.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .hasSize(7)
                .noneMatch(c -> c.getName().equals("Bone Dragon"));
    }

    @Test
    @DisplayName("Graveyard ability cannot be activated without seven other cards to exile")
    void graveyardAbilityRequiresSevenOtherCards() {
        List<com.github.laxika.magicalvibes.model.Card> graveyard = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            graveyard.add(new GrizzlyBears());
        }
        graveyard.add(new BoneDragon());
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 6))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Bone Dragon");
    }
}
