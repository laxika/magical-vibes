package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GuardiansMagemark;
import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.cards.o.OstiaryThrull;
import com.github.laxika.magicalvibes.cards.p.PilloryOfTheSleepless;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NecromancersMagemark.class, PilloryOfTheSleepless.class, OstiaryThrull.class,
        Mortify.class, GuardiansMagemark.class})
class NecromancersMagemarkTest extends BaseCardTest {

    @Test
    @DisplayName("Attaches to a creature when cast")
    void resolvesAttachedToTargetCreature() {
        Permanent creature = addCreatureReady(player1, new OstiaryThrull());
        harness.setHand(player1, List.of(new NecromancersMagemark()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof NecromancersMagemark
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GuardiansMagemark());
        harness.setHand(player1, List.of(new NecromancersMagemark()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Boosts each enchanted creature its controller controls")
    void boostsEnchantedCreaturesYouControl() {
        Permanent first = addCreatureReady(player1, new OstiaryThrull());
        Permanent second = addCreatureReady(player1, new OstiaryThrull());
        Permanent unenchanted = addCreatureReady(player1, new OstiaryThrull());
        Permanent opponentEnchanted = addCreatureReady(player2, new OstiaryThrull());
        attach(new NecromancersMagemark(), first, player1);
        attach(new PilloryOfTheSleepless(), second, player1);
        attach(new PilloryOfTheSleepless(), opponentEnchanted, player1);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, unenchanted)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unenchanted)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentEnchanted)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentEnchanted)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns any enchanted creature you control to its owner's hand instead of letting it die")
    void returnsEnchantedCreatureYouControlToHand() {
        Permanent protectedCreature = addCreatureReady(player1, new OstiaryThrull());
        Permanent dyingCreature = addCreatureReady(player1, new OstiaryThrull());
        attach(new NecromancersMagemark(), protectedCreature, player1);
        attach(new PilloryOfTheSleepless(), dyingCreature, player1);
        Card dyingCard = dyingCreature.getCard();

        destroyWithMortify(dyingCreature);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(dyingCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(dyingCard.getId()));
    }

    @Test
    @DisplayName("Does not replace the death of an unenchanted creature")
    void doesNotReplaceUnenchantedCreature() {
        Permanent protectedCreature = addCreatureReady(player1, new OstiaryThrull());
        Permanent dyingCreature = addCreatureReady(player1, new OstiaryThrull());
        attach(new NecromancersMagemark(), protectedCreature, player1);
        Card dyingCard = dyingCreature.getCard();

        destroyWithMortify(dyingCreature);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(dyingCard.getId()));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getId().equals(dyingCard.getId()));
    }

    @Test
    @DisplayName("Does not replace the death of an enchanted creature controlled by an opponent")
    void doesNotReplaceOpponentControlledCreature() {
        Permanent dyingCreature = addCreatureReady(player2, new OstiaryThrull());
        attach(new NecromancersMagemark(), dyingCreature, player1);
        Card dyingCard = dyingCreature.getCard();

        destroyWithMortify(dyingCreature);

        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(card -> card.getId().equals(dyingCard.getId()));
        assertThat(gd.playerHands.get(player2.getId())).noneMatch(card -> card.getId().equals(dyingCard.getId()));
    }

    private void attach(Card auraCard, Permanent creature, Player controller) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, auraCard);
        aura.setAttachedTo(creature.getId());
    }

    private void destroyWithMortify(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Mortify()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }
}
