package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFloodOfMars.class, Forest.class, GrizzlyBears.class, DryadArbor.class})
class TheFloodOfMarsTest extends BaseCardTest {

    @Test
    @DisplayName("The attack trigger targets another creature or land")
    void attackTriggerOffersAnotherCreatureOrLand() {
        Permanent flood = addCreatureReady(player1, new TheFloodOfMars());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(creature.getId(), land.getId());
        assertThat(choice.validIds()).doesNotContain(flood.getId());
    }

    @Test
    @DisplayName("The attack trigger floods and copies the target creature")
    void floodsAndCopiesTargetCreature() {
        addCreatureReady(player1, new TheFloodOfMars());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(target.getCard().getName()).isEqualTo("The Flood of Mars");
        assertThat(target.getCard().getPower()).isEqualTo(3);
        assertThat(target.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The attack trigger floods a land and adds Island to its types")
    void floodsAndAddsIslandToTargetLand() {
        addCreatureReady(player1, new TheFloodOfMars());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(gqs.effectiveLandTypes(gd, target))
                .contains(CardSubtype.FOREST, CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("A flooded land remains an Island after its flood counter is removed")
    void islandEffectDoesNotDependOnFloodCounter() {
        addCreatureReady(player1, new TheFloodOfMars());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        target.setCounterCount(CounterType.FLOOD, 0);

        assertThat(gqs.effectiveLandTypes(gd, target))
                .contains(CardSubtype.FOREST, CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("A creature land becomes a copy without also becoming an Island")
    void creatureLandCopiesBeforeLandConditionIsChecked() {
        addCreatureReady(player1, new TheFloodOfMars());
        Permanent target = addCreatureReady(player2, new DryadArbor());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(target.getCard().getName()).isEqualTo("The Flood of Mars");
        assertThat(gqs.isLand(gd, target)).isFalse();
        assertThat(gqs.effectiveLandTypes(gd, target)).doesNotContain(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("The target copies the source even if the source leaves before resolution")
    void copiesSourceUsingLastKnownInformation() {
        Permanent source = addCreatureReady(player1, new TheFloodOfMars());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getOriginalCard());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(target.getCard().getName()).isEqualTo("The Flood of Mars");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Flooding a defending land enables islandwalk")
    void floodedDefendingLandPreventsBlocking() {
        Permanent source = addCreatureReady(player1, new TheFloodOfMars());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThat(bls.canBlockAttacker(gd, blocker, source,
                gd.playerBattlefields.get(player2.getId()))).isTrue();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, blocker, source,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("A copied creature retains the copy and can spread it after losing its flood counter")
    void copiedCreatureCanSpreadCopyWithoutCounter() {
        Permanent source = addCreatureReady(player1, new TheFloodOfMars());
        Permanent copied = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, copied.getId());
        harness.passBothPriorities();
        copied.setCounterCount(CounterType.FLOOD, 0);
        source.tap();

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(copied.getCard().getName()).isEqualTo("The Flood of Mars");
        assertThat(target.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(target.getCard().getName()).isEqualTo("The Flood of Mars");
    }
}
