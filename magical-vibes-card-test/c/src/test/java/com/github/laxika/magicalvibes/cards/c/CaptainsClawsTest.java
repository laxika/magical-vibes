package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NissaVoiceOfZendikar;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainsClaws.class, GrizzlyBears.class, NissaVoiceOfZendikar.class})
class CaptainsClawsTest extends BaseCardTest {

    @Test
    @DisplayName("Equip {1} attaches Captain's Claws and gives the creature +1/+0")
    void equipBoostsCreature() {
        harness.addToBattlefield(player1, new CaptainsClaws());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking with the equipped creature creates a tapped and attacking Kor Ally")
    void attackCreatesTappedAndAttackingKorAlly() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent claws = harness.addToBattlefieldAndReturn(player1, new CaptainsClaws());
        claws.setAttachedTo(creature.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            resolveAllTriggers();
        });

        Permanent token = findPermanent(player1, "Kor Ally");
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.KOR, CardSubtype.ALLY);
    }

    @Test
    @DisplayName("An unattached Captain's Claws does not trigger when a creature attacks")
    void unattachedClawsDoesNotTrigger() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CaptainsClaws());

        declareAttackers(player1, List.of(0));

        assertThat(findPermanents(player1, "Kor Ally")).isEmpty();
    }

    @Test
    @DisplayName("Claws controlled by the defending player creates a tapped token outside combat")
    void defendingControllerCreatesTokenThatIsNotAttacking() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent claws = harness.addToBattlefieldAndReturn(player1, new CaptainsClaws());
        claws.setAttachedTo(creature.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            resolveAllTriggers();
        });

        Permanent token = findPermanent(player1, "Kor Ally");
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isFalse();
        assertThat(findPermanents(player2, "Kor Ally")).isEmpty();
    }

    @Test
    @DisplayName("The Kor Ally can attack a planeswalker independently of the equipped creature")
    void tokenCanAttackDefendingPlaneswalker() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent claws = harness.addToBattlefieldAndReturn(player1, new CaptainsClaws());
        claws.setAttachedTo(creature.getId());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new NissaVoiceOfZendikar());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.isAwaitingInput()).isTrue();
            harness.handlePermanentChosen(player1, planeswalker.getId());
        });

        Permanent token = findPermanent(player1, "Kor Ally");
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(planeswalker.getId());
        assertThat(creature.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The attack trigger still creates its token after Captain's Claws leaves")
    void triggerSurvivesEquipmentLeavingBattlefield() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent claws = harness.addToBattlefieldAndReturn(player1, new CaptainsClaws());
        claws.setAttachedTo(creature.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            gd.playerBattlefields.get(player1.getId()).remove(claws);
            gd.playerGraveyards.get(player1.getId()).add(claws.getCard());
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Kor Ally")).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }
}
