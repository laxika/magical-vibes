package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CharcoalDiamond;
import com.github.laxika.magicalvibes.cards.c.CombatCourier;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GarruksPackleader;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhalanxVanguard;
import com.github.laxika.magicalvibes.cards.w.WarlordsElite;
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

@CardUsed({Recommission.class, GrizzlyBears.class, CharcoalDiamond.class, AirElemental.class,
        CombatCourier.class, Disenchant.class, GarruksPackleader.class, PhalanxVanguard.class,
        WarlordsElite.class})
class RecommissionTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a low mana value creature with a +1/+1 counter")
    void returnsCreatureWithCounter() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Recommission()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, creature.getName());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns a low mana value artifact without a +1/+1 counter")
    void returnsArtifactWithoutCounter() {
        Card artifact = new CharcoalDiamond();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new Recommission()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, artifact.getName());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot target a card with mana value greater than three")
    void cannotTargetHighManaValueCard() {
        Card creature = new AirElemental();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Recommission()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsArtifactCreatureWithExactlyOneCounter() {
        Card creature = new CombatCourier();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Recommission()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, creature.getName())
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    void returnsManaValueThreeCreatureWithoutPayingItsCastingCosts() {
        Card creature = new WarlordsElite();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Recommission()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, creature.getName())
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotTargetOpponentsGraveyard() {
        Card creature = new CombatCourier();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new Recommission()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetLowManaValueInstant() {
        Card instant = new Disenchant();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new Recommission()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReturnTargetThatLeavesGraveyardBeforeResolution() {
        Card creature = new CombatCourier();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Recommission()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, creature.getId());

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Recommission");
    }

    @Test
    void entryTriggersSeeTheAdditionalCounter() {
        harness.addToBattlefield(player1, new GarruksPackleader());
        Card creature = new PhalanxVanguard();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new CombatCourier()));
        harness.setHand(player1, List.of(new Recommission()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
