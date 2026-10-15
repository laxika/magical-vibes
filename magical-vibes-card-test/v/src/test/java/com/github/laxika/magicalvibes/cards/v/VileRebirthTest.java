package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VileRebirth.class, WalkingCorpse.class, Murder.class})
class VileRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target creature card from an opponent's graveyard and creates a Zombie token")
    void exilesFromOpponentGraveyardAndCreatesToken() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new VileRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player2, "Walking Corpse");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));
        harness.assertOnBattlefield(player1, "Zombie");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        var zombie = findPermanent(player1, "Zombie").getCard();
        assertThat(zombie.isToken()).isTrue();
        assertThat(zombie.getPower()).isEqualTo(2);
        assertThat(zombie.getToughness()).isEqualTo(2);
        assertThat(zombie.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
        harness.assertNotOnBattlefield(player2, "Zombie");
    }

    @Test
    @DisplayName("Can exile a creature card from your own graveyard")
    void exilesFromOwnGraveyard() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new VileRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player1, "Walking Corpse");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNoncreatureCard() {
        Card noncreature = new Murder();
        harness.setGraveyard(player2, List.of(noncreature));
        harness.setHand(player1, List.of(new VileRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast without a graveyard target")
    void cannotCastWithoutTarget() {
        harness.setHand(player1, List.of(new VileRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creates no token when another Vile Rebirth exiles the only target first")
    void noTokenWhenTargetExiledInResponse() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new VileRebirth(), new VileRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Zombie");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Vile Rebirth")).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a creature card in a hand")
    void cannotTargetCreatureInHand() {
        Card creature = new WalkingCorpse();
        harness.setHand(player2, List.of(creature));
        harness.setHand(player1, List.of(new VileRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
