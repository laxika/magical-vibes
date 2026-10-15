package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.b.BronzeplateBoar;
import com.github.laxika.magicalvibes.cards.u.UnstoppableOgre;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImperialSubduer.class, UnstoppableOgre.class, BronzeplateBoar.class, AmoeboidChangeling.class})
class ImperialSubduerTest extends BaseCardTest {

    @Test
    void creatureThatGainsSamuraiAndWarriorTypesTriggersWhenAttackingAlone() {
        addCreatureReady(player1, new ImperialSubduer());
        Permanent attacker = addCreatureReady(player1, new BronzeplateBoar());
        addCreatureReady(player1, new AmoeboidChangeling());
        Permanent victim = addCreatureReady(player2, new BronzeplateBoar());

        harness.activateAbility(player1, 2, 0, null, attacker.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(victim.getId());
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    void samuraiThatLosesAllCreatureTypesDoesNotTriggerWhenAttackingAlone() {
        Permanent attacker = addCreatureReady(player1, new ImperialSubduer());
        addCreatureReady(player1, new AmoeboidChangeling());
        Permanent victim = addCreatureReady(player2, new BronzeplateBoar());

        harness.activateAbility(player1, 1, 1, null, attacker.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    void opposingWarriorAttackingAloneDoesNotTriggerSubduer() {
        Permanent subduer = addCreatureReady(player1, new ImperialSubduer());
        addCreatureReady(player2, new UnstoppableOgre());

        declareAttackers(player2, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(subduer.isTapped()).isFalse();
    }

    @Test
    void alreadyTappedCreatureIsStillALegalTarget() {
        addCreatureReady(player1, new ImperialSubduer());
        Permanent victim = addCreatureReady(player2, new BronzeplateBoar());
        victim.tap();

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(victim.getId());
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attackingWithoutALegalTargetDoesNotLeaveATargetChoicePending() {
        addCreatureReady(player1, new ImperialSubduer());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void samuraiAttackingAloneTapsTargetOpponentCreature() {
        addCreatureReady(player1, new ImperialSubduer());
        Permanent victim = addCreatureReady(player2, new BronzeplateBoar());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(victim.getId());
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    void warriorAttackingAloneTapsTargetOpponentCreature() {
        addCreatureReady(player1, new ImperialSubduer());
        addCreatureReady(player1, new UnstoppableOgre());
        Permanent victim = addCreatureReady(player2, new BronzeplateBoar());

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    void triggerDoesNotFireForNonSamuraiOrWarrior() {
        addCreatureReady(player1, new ImperialSubduer());
        addCreatureReady(player1, new BronzeplateBoar());
        Permanent victim = addCreatureReady(player2, new BronzeplateBoar());

        declareAttackers(player1, List.of(1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    void triggerDoesNotFireWhenSamuraiOrWarriorDoesNotAttackAlone() {
        addCreatureReady(player1, new ImperialSubduer());
        addCreatureReady(player1, new UnstoppableOgre());
        addCreatureReady(player1, new BronzeplateBoar());
        Permanent victim = addCreatureReady(player2, new BronzeplateBoar());

        declareAttackers(player1, List.of(1, 2));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    void triggerCannotTargetYourOwnCreature() {
        addCreatureReady(player1, new ImperialSubduer());
        Permanent ownCreature = addCreatureReady(player1, new BronzeplateBoar());
        Permanent victim = addCreatureReady(player2, new BronzeplateBoar());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(victim.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
