package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.z.ZombieGoliath;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisturbingPlotTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature card from graveyard to hand")
    void returnsTargetCreatureToHand() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DisturbingPlot()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Can target an opponent's graveyard; the card returns to its owner's hand")
    void returnsFromOpponentGraveyardToOwnersHand() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new DisturbingPlot()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // "to its owner's hand" — the opponent's card goes to the opponent's hand, not the caster's.
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot target a non-creature card in the graveyard")
    void cannotTargetNonCreature() {
        Card enchantment = new Pacifism();
        harness.setGraveyard(player1, List.of(enchantment));
        harness.setHand(player1, List.of(new DisturbingPlot()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Conspire taps two color-sharing creatures and queues a copy of the spell")
    void conspireTapsCreaturesAndQueuesCopy() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DisturbingPlot()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        Permanent zombie1 = addCreatureReady(player1, new ScatheZombies());
        Permanent zombie2 = addCreatureReady(player1, new ZombieGoliath());

        harness.castWithConspire(player1, 0, creature.getId(), List.of(zombie1.getId(), zombie2.getId()));

        GameData gd = harness.getGameData();
        assertThat(zombie1.isTapped()).isTrue();
        assertThat(zombie2.isTapped()).isTrue();

        // The spell plus one conspire copy trigger are on the stack.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack).anyMatch(e -> e.getEffectsToResolve().stream()
                .anyMatch(fx -> fx instanceof CopyControllerCastSpellEffect));
    }

    @Test
    @DisplayName("Conspire is rejected when a chosen creature does not share a color with the spell")
    void conspireRejectsNonBlackCreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DisturbingPlot()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        Permanent zombie = addCreatureReady(player1, new ScatheZombies());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // green, does not share black

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, creature.getId(),
                List.of(zombie.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
