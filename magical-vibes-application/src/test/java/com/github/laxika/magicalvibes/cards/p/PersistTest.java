package com.github.laxika.magicalvibes.cards.p;
import java.util.UUID;

import com.github.laxika.magicalvibes.cards.c.CaptainSisay;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FesterhideBoar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.g.GarruksPackleader;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Persist.class, GrizzlyBears.class, CaptainSisay.class, LlanowarElves.class,
        FesterhideBoar.class, GarruksPackleader.class})
class PersistTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a nonlegendary creature with a -1/-1 counter")
    void returnsNonlegendaryCreatureWithCounter() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Persist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, creature.getName());
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot target a legendary creature")
    void cannotTargetLegendaryCreature() {
        Card creature = new CaptainSisay();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Persist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new Persist()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature card")
    void cannotTargetNoncreatureCard() {
        Card target = new Persist();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new Persist()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires a target even when the graveyard is empty")
    void cannotCastWithoutTarget() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new Persist()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not return a target that has left the graveyard")
    void targetLeavingGraveyardPreventsReturn() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Persist()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, creature.getId());

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(creature.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The entry counter causes a one-toughness creature to die")
    void entryCounterCausesZeroToughnessDeath() {
        Card creature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Persist()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.get(player1.getId()))
                .contains(creature.getId());
    }

    @Test
    @DisplayName("Entry triggers see the creature with Persist's counter already on it")
    void entryTriggerSeesReducedPower() {
        harness.addToBattlefield(player1, new GarruksPackleader());
        Card creature = new FesterhideBoar();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Persist()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, creature.getName())
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
