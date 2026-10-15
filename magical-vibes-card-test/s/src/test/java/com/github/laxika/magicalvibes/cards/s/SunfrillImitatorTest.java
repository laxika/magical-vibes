package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AureliaTheWarleader;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PygmyAllosaurus;
import com.github.laxika.magicalvibes.cards.r.RampagingBrontodon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunfrillImitator.class, PygmyAllosaurus.class, GrizzlyBears.class,
        AureliaTheWarleader.class, RampagingBrontodon.class})
class SunfrillImitatorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking Sunfrill Imitator targets another Dinosaur you control")
    void targetsAnotherDinosaurYouControl() {
        Permanent imitator = addCreatureReady(player1, new SunfrillImitator());
        Permanent ownDinosaur = addCreatureReady(player1, new PygmyAllosaurus());
        Permanent opposingDinosaur = addCreatureReady(player2, new PygmyAllosaurus());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownDinosaur.getId())
                .doesNotContain(imitator.getId(), opposingDinosaur.getId());
    }

    @Test
    @DisplayName("Accepting the attack trigger permanently copies the target and keeps the name")
    void copiesTargetAndKeepsName() {
        Permanent imitator = addCreatureReady(player1, new SunfrillImitator());
        Permanent dinosaur = addCreatureReady(player1, new PygmyAllosaurus());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, dinosaur.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(imitator.getCard().getName()).isEqualTo("Sunfrill Imitator");
        assertThat(imitator.getCard().getPower()).isEqualTo(dinosaur.getCard().getPower());
        assertThat(imitator.getCard().getToughness()).isEqualTo(dinosaur.getCard().getToughness());
    }

    @Test
    @DisplayName("Declining the attack trigger leaves Sunfrill Imitator unchanged")
    void canDeclineCopy() {
        Permanent imitator = addCreatureReady(player1, new SunfrillImitator());
        Permanent dinosaur = addCreatureReady(player1, new PygmyAllosaurus());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, dinosaur.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(imitator.getCard().getName()).isEqualTo("Sunfrill Imitator");
    }

    @Test
    @DisplayName("The copied Sunfrill Imitator retains its attack ability")
    void retainsAttackAbilityAfterCopying() {
        addCreatureReady(player1, new AureliaTheWarleader());
        Permanent imitator = addCreatureReady(player1, new SunfrillImitator());
        Permanent firstDinosaur = addCreatureReady(player1, new PygmyAllosaurus());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, firstDinosaur.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent secondDinosaur = addCreatureReady(player1, new PygmyAllosaurus());
        harness.passBothPriorities();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(firstDinosaur.getId(), secondDinosaur.getId())
                .doesNotContain(imitator.getId());
    }

    @Test
    @DisplayName("The attack ability does not target a non-Dinosaur or an opponent's Dinosaur")
    void rejectsInvalidTargets() {
        addCreatureReady(player1, new SunfrillImitator());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new PygmyAllosaurus());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Copying preserves the imitator's counters without copying the target's counters")
    void preservesOwnCountersWithoutCopyingTargetCounters() {
        Permanent imitator = addCreatureReady(player1, new SunfrillImitator());
        Permanent dinosaur = addCreatureReady(player1, new RampagingBrontodon());
        imitator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        dinosaur.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, dinosaur.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, imitator)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, imitator)).isEqualTo(8);
        assertThat(imitator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The copy ability fizzles when the target leaves the battlefield")
    void doesNotCopyMissingTarget() {
        Permanent imitator = addCreatureReady(player1, new SunfrillImitator());
        Permanent dinosaur = addCreatureReady(player1, new RampagingBrontodon());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, dinosaur.getId());
        gd.playerBattlefields.get(player1.getId()).remove(dinosaur);
        gd.playerGraveyards.get(player1.getId()).add(dinosaur.getOriginalCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gqs.getEffectivePower(gd, imitator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, imitator)).isEqualTo(3);
    }

    @Test
    @DisplayName("The copy ability fizzles when the target is no longer controlled by its controller")
    void doesNotCopyTargetThatChangesController() {
        Permanent imitator = addCreatureReady(player1, new SunfrillImitator());
        Permanent dinosaur = addCreatureReady(player1, new RampagingBrontodon());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, dinosaur.getId());
        gd.playerBattlefields.get(player1.getId()).remove(dinosaur);
        gd.playerBattlefields.get(player2.getId()).add(dinosaur);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gqs.getEffectivePower(gd, imitator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, imitator)).isEqualTo(3);
    }
}
