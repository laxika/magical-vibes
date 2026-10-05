package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.j.Jump;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.RuxaPatientProfessor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MuragandaEldrazi.class, LlanowarElves.class, Mountain.class,
        MuragandaPetroglyphs.class, Jump.class, Counterspell.class, RuxaPatientProfessor.class})
class MuragandaEldraziTest extends BaseCardTest {

    @Test
    void putsPrimevalCounterOnTargetCreatureAndRemovesItsAbilities() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new MuragandaEldrazi()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PRIMEVAL)).isEqualTo(1);
        assertThat(gqs.hasLostAllAbilities(gd, target)).isTrue();
        assertThat(gqs.canActivateManaAbility(gd, target)).isFalse();
        harness.assertNotOnBattlefield(player1, "Muraganda Eldrazi");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Muraganda Eldrazi");
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new MuragandaEldrazi()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(land.getCounterCount(CounterType.PRIMEVAL)).isZero();
        assertThat(creature.getCounterCount(CounterType.PRIMEVAL)).isEqualTo(1);
    }

    @Test
    void canBeCastWithoutAnyCreatureToTarget() {
        harness.setHand(player1, List.of(new MuragandaEldrazi()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Muraganda Eldrazi");
        assertThat(findPermanent(player1, "Muraganda Eldrazi").getCounterCount(CounterType.PRIMEVAL)).isZero();
    }

    @Test
    void dewordedCreatureReceivesNoAbilitiesBonus() {
        Permanent eldrazi = harness.addToBattlefieldAndReturn(player1, new MuragandaEldrazi());
        harness.addToBattlefield(player2, new MuragandaPetroglyphs());

        assertThat(gqs.getEffectivePower(gd, eldrazi)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, eldrazi)).isEqualTo(7);
        assertThat(eldrazi.getCounterCount(CounterType.PRIMEVAL)).isZero();
    }

    @Test
    void dewordedCreatureCanBeReturnedFromGraveyardByRuxa() {
        MuragandaEldrazi eldrazi = new MuragandaEldrazi();
        harness.setGraveyard(player1, List.of(eldrazi));
        harness.setHand(player1, List.of(new RuxaPatientProfessor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eldrazi.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eldrazi.getId()));
        harness.passBothPriorities();
        harness.assertInHand(player1, "Muraganda Eldrazi");
        harness.assertNotInGraveyard(player1, "Muraganda Eldrazi");
    }

    @Test
    void primevalCounterPreventsLaterAbilityGain() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new MuragandaEldrazi(), new Jump()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PRIMEVAL)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gqs.canActivateManaAbility(gd, target)).isFalse();
    }

    @Test
    void castTriggerResolvesEvenWhenCreatureSpellIsCountered() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        MuragandaEldrazi eldrazi = new MuragandaEldrazi();
        harness.setHand(player1, List.of(eldrazi));
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player2, 0, eldrazi.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Muraganda Eldrazi");
        harness.assertNotOnBattlefield(player1, "Muraganda Eldrazi");
        assertThat(target.getCounterCount(CounterType.PRIMEVAL)).isEqualTo(1);
        assertThat(gqs.canActivateManaAbility(gd, target)).isFalse();
    }
}
