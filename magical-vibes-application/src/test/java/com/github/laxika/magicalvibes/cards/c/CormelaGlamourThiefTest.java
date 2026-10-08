package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DigUpTheBody;
import com.github.laxika.magicalvibes.cards.g.Goldhound;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.o.ObscuraInitiate;
import com.github.laxika.magicalvibes.cards.s.Strangle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CormelaGlamourThief.class, Strangle.class, ObscuraInitiate.class,
        Murder.class, DigUpTheBody.class, Goldhound.class})
class CormelaGlamourThiefTest extends BaseCardTest {

    @Test
    void tapAddsThreeColorsOfInstantOrSorceryOnlyMana() {
        Permanent cormela = addCreatureReady(player1, new CormelaGlamourThief());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Goldhound());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.BLACK)).isEqualTo(1);
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.getTotalAllMana()).isEqualTo(3);

        harness.setHand(player1, List.of(new Strangle()));
        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Goldhound");
        assertThat(cormela.isTapped()).isTrue();
    }

    @Test
    void manaAbilityCannotBeActivatedWithoutPayingOneMana() {
        Permanent cormela = harness.addToBattlefieldAndReturn(player1, new CormelaGlamourThief());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cormela.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.BLUE))
                .isZero();
    }

    @Test
    void hasteAllowsManaAbilityImmediatelyAfterEntering() {
        Permanent cormela = harness.addToBattlefieldAndReturn(player1, new CormelaGlamourThief());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(cormela.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    void deathTriggerReturnsSorceryFromOnlyControllersGraveyard() {
        Permanent cormela = harness.addToBattlefieldAndReturn(player1, new CormelaGlamourThief());
        Strangle ownSpell = new Strangle();
        Strangle opponentsSpell = new Strangle();
        harness.setGraveyard(player1, List.of(ownSpell));
        harness.setGraveyard(player2, List.of(opponentsSpell));

        destroyCormela(cormela);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownSpell.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownSpell.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Strangle");
        harness.assertInGraveyard(player2, "Strangle");
    }

    @Test
    void deathTriggerDoesNotReturnAnotherCardIfItsTargetLeavesGraveyard() {
        Permanent cormela = harness.addToBattlefieldAndReturn(player1, new CormelaGlamourThief());
        Strangle target = new Strangle();
        DigUpTheBody otherSpell = new DigUpTheBody();
        harness.setGraveyard(player1, List.of(target, otherSpell));

        destroyCormela(cormela);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> !card.getId().equals(target.getId())).toList());
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Strangle");
        harness.assertNotInHand(player1, "Dig Up the Body");
        harness.assertInGraveyard(player1, "Dig Up the Body");
    }

    @Test
    void restrictedManaCanPayForInstantSpells() {
        addCreatureReady(player1, new CormelaGlamourThief());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Goldhound());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player1, List.of(new Murder()));

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Goldhound");
    }

    @Test
    void deathTriggerWithNoEligibleCardsReturnsNothing() {
        harness.setHand(player1, List.of());
        Permanent cormela = harness.addToBattlefieldAndReturn(player1, new CormelaGlamourThief());
        harness.setGraveyard(player1, List.of(new Goldhound()));

        destroyCormela(cormela);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        if (choice != null) {
            assertThat(choice.validCardIds()).isEmpty();
            harness.handleMultipleCardsChosen(player1, List.of());
        }
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cormela, Glamour Thief");
        harness.assertInGraveyard(player1, "Goldhound");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void instantOrSorceryOnlyManaCannotCastCreatureSpells() {
        addCreatureReady(player1, new CormelaGlamourThief());
        harness.setHand(player1, List.of(new ObscuraInitiate()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deathTriggerReturnsTargetInstantOrSorceryFromGraveyard() {
        Permanent cormela = harness.addToBattlefieldAndReturn(player1, new CormelaGlamourThief());
        DigUpTheBody opt = new DigUpTheBody();
        Goldhound bears = new Goldhound();
        harness.setGraveyard(player1, List.of(opt, bears));

        destroyCormela(cormela);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(opt.getId());

        harness.handleMultipleCardsChosen(player1, List.of(opt.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Dig Up the Body");
        harness.assertInGraveyard(player1, "Goldhound");
    }

    @Test
    void deathTriggerCanDeclineItsOptionalReturn() {
        Permanent cormela = harness.addToBattlefieldAndReturn(player1, new CormelaGlamourThief());
        DigUpTheBody opt = new DigUpTheBody();
        harness.setGraveyard(player1, List.of(opt));

        destroyCormela(cormela);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dig Up the Body");
        harness.assertNotInHand(player1, "Dig Up the Body");
    }

    private void destroyCormela(Permanent cormela) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, cormela.getId());
    }
}
