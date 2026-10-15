package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.u.UnrulyMob;
import com.github.laxika.magicalvibes.cards.c.Consider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiregrafRebirth.class, UnrulyMob.class, Consider.class})
class DiregrafRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature card from your graveyard to the battlefield")
    void returnsTargetCreatureFromGraveyard() {
        Card creature = new UnrulyMob();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DiregrafRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Costs one less for each creature that died this turn")
    void reducesCostForCreatureDeathsByAnyPlayer() {
        Card creature = new UnrulyMob();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DiregrafRebirth()));
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 3, Integer::sum);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Flashback returns the creature and exiles Diregraf Rebirth")
    void flashbackReturnsCreatureAndExilesSpell() {
        DiregrafRebirth spell = new DiregrafRebirth();
        Card creature = new UnrulyMob();
        harness.setGraveyard(player1, List.of(spell, creature));
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 5, Integer::sum);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveFlashback(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    void cannotTargetCreatureInOpponentsGraveyard() {
        Card creature = new UnrulyMob();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new DiregrafRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    void dyingCreatureReducesCostOfItsOwnReanimation() {
        Card creature = new UnrulyMob();
        harness.addToBattlefield(player1, creature);
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(
                gd, gd.playerBattlefields.get(player1.getId()).getFirst());
        harness.setHand(player1, List.of(new DiregrafRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Unruly Mob");
        harness.assertNotInGraveyard(player1, "Unruly Mob");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void partialReductionCountsBothPlayersDeaths() {
        Card creature = new UnrulyMob();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DiregrafRebirth()));
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Unruly Mob");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void excessDeathsDoNotReduceColoredManaRequirement() {
        Card creature = new UnrulyMob();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DiregrafRebirth()));
        gd.creatureDeathCountThisTurn.put(player2.getId(), 10);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Diregraf Rebirth");
        harness.assertInGraveyard(player1, "Unruly Mob");
    }

    @Test
    void flashbackWithoutDeathsRequiresFullFlashbackCost() {
        DiregrafRebirth spell = new DiregrafRebirth();
        Card creature = new UnrulyMob();
        harness.setGraveyard(player1, List.of(spell, creature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveFlashback(player1, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Unruly Mob");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    void flashbackExilesSpellWhenTargetLeavesGraveyard() {
        DiregrafRebirth spell = new DiregrafRebirth();
        Card creature = new UnrulyMob();
        harness.setGraveyard(player1, List.of(spell, creature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFlashback(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Unruly Mob");
        harness.assertInHand(player1, "Unruly Mob");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
        harness.assertNotInGraveyard(player1, "Diregraf Rebirth");
    }

    @Test
    @DisplayName("Cannot target a noncreature card in your graveyard")
    void cannotTargetNoncreatureCard() {
        Card noncreature = new Consider();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setHand(player1, List.of(new DiregrafRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
