package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreadReturn.class, AshcoatBear.class, Cancel.class, Mountain.class})
class DreadReturnTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature from your graveyard to the battlefield")
    void returnsCreatureFromGraveyardToBattlefield() {
        Card creature = new AshcoatBear();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DreadReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature card in your graveyard")
    void cannotTargetNoncreatureCard() {
        Card noncreature = new Cancel();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setHand(player1, List.of(new DreadReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card opponentCreature = new AshcoatBear();
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new DreadReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(opponentCreature);
    }

    @Test
    @DisplayName("Flashback sacrifices three creatures, returns the target, and exiles the spell")
    void flashbackSacrificesThreeCreatures() {
        Permanent first = addCreatureReady(player1, new AshcoatBear());
        Permanent second = addCreatureReady(player1, new AshcoatBear());
        Permanent third = addCreatureReady(player1, new AshcoatBear());
        Card creature = new AshcoatBear();
        DreadReturn spell = new DreadReturn();
        harness.setGraveyard(player1, List.of(spell, creature));

        harness.castFromGraveyardWithSacrifices(player1, 0, creature.getId(),
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> List.of(first.getId(), second.getId(), third.getId())
                        .contains(permanent.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getCard().getId(), second.getCard().getId(), third.getCard().getId());
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }

    @Test
    @DisplayName("Flashback requires exactly three creatures to sacrifice")
    void flashbackRequiresThreeCreatures() {
        Permanent first = addCreatureReady(player1, new AshcoatBear());
        Permanent second = addCreatureReady(player1, new AshcoatBear());
        Card creature = new AshcoatBear();
        DreadReturn spell = new DreadReturn();
        harness.setGraveyard(player1, List.of(spell, creature));

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifices(player1, 0, creature.getId(),
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Dread Return");
    }

    @Test
    @DisplayName("Flashback cannot sacrifice more than three creatures")
    void flashbackCannotSacrificeFourCreatures() {
        Permanent first = addCreatureReady(player1, new AshcoatBear());
        Permanent second = addCreatureReady(player1, new AshcoatBear());
        Permanent third = addCreatureReady(player1, new AshcoatBear());
        Permanent fourth = addCreatureReady(player1, new AshcoatBear());
        Card creature = new AshcoatBear();
        DreadReturn spell = new DreadReturn();
        harness.setGraveyard(player1, List.of(spell, creature));

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifices(player1, 0, creature.getId(),
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Dread Return");
    }

    @Test
    @DisplayName("Flashback cannot sacrifice a noncreature permanent")
    void flashbackRequiresCreatures() {
        Permanent first = addCreatureReady(player1, new AshcoatBear());
        Permanent second = addCreatureReady(player1, new AshcoatBear());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Card creature = new AshcoatBear();
        harness.setGraveyard(player1, List.of(new DreadReturn(), creature));

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifices(player1, 0, creature.getId(),
                List.of(first.getId(), second.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Countering flashback exiles Dread Return without refunding its sacrifices")
    void counteredFlashbackExilesSpell() {
        Permanent first = addCreatureReady(player1, new AshcoatBear());
        Permanent second = addCreatureReady(player1, new AshcoatBear());
        Permanent third = addCreatureReady(player1, new AshcoatBear());
        Card creature = new AshcoatBear();
        DreadReturn spell = new DreadReturn();
        harness.setGraveyard(player1, List.of(spell, creature));
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromGraveyardWithSacrifices(player1, 0, creature.getId(),
                List.of(first.getId(), second.getId(), third.getId()));
        harness.castAndResolveInstant(player2, 0, spell.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(creature, first.getCard(), second.getCard(), third.getCard());
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }

    @Test
    @DisplayName("Flashback with a target that leaves the graveyard still exiles the spell")
    void flashbackWithMissingTargetExilesSpell() {
        Permanent first = addCreatureReady(player1, new AshcoatBear());
        Permanent second = addCreatureReady(player1, new AshcoatBear());
        Permanent third = addCreatureReady(player1, new AshcoatBear());
        Card creature = new AshcoatBear();
        DreadReturn spell = new DreadReturn();
        harness.setGraveyard(player1, List.of(spell, creature));

        harness.castFromGraveyardWithSacrifices(player1, 0, creature.getId(),
                List.of(first.getId(), second.getId(), third.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(creature);
        gd.playerHands.get(player1.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(first.getCard(), second.getCard(), third.getCard());
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }

    @Test
    @DisplayName("Flashback cannot target a creature being sacrificed to pay its cost")
    void cannotReturnCreatureSacrificedForFlashback() {
        Permanent first = addCreatureReady(player1, new AshcoatBear());
        Permanent second = addCreatureReady(player1, new AshcoatBear());
        Permanent third = addCreatureReady(player1, new AshcoatBear());
        DreadReturn spell = new DreadReturn();
        harness.setGraveyard(player1, List.of(spell));

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifices(player1, 0,
                first.getCard().getId(), List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactlyInAnyOrder(first, second, third);
    }
}
