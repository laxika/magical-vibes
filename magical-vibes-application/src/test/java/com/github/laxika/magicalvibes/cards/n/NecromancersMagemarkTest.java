package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
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

@CardUsed({NecromancersMagemark.class, Pacifism.class, GrizzlyBears.class, DoomBlade.class})
class NecromancersMagemarkTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts each enchanted creature its controller controls")
    void boostsEnchantedCreaturesYouControl() {
        Permanent first = addCreature(player1);
        Permanent second = addCreature(player1);
        Permanent unenchanted = addCreature(player1);
        attach(new NecromancersMagemark(), first, player1);
        attach(new Pacifism(), second, player1);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, unenchanted)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unenchanted)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns any enchanted creature you control to its owner's hand instead of letting it die")
    void returnsEnchantedCreatureYouControlToHand() {
        Permanent protectedCreature = addCreature(player1);
        Permanent dyingCreature = addCreature(player1);
        attach(new NecromancersMagemark(), protectedCreature, player1);
        attach(new Pacifism(), dyingCreature, player1);
        Card dyingCard = dyingCreature.getCard();

        destroyWithDoomBlade(dyingCreature);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(dyingCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(dyingCard.getId()));
    }

    @Test
    @DisplayName("Does not replace the death of an unenchanted creature")
    void doesNotReplaceUnenchantedCreature() {
        Permanent protectedCreature = addCreature(player1);
        Permanent dyingCreature = addCreature(player1);
        attach(new NecromancersMagemark(), protectedCreature, player1);
        Card dyingCard = dyingCreature.getCard();

        destroyWithDoomBlade(dyingCreature);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(dyingCard.getId()));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getId().equals(dyingCard.getId()));
    }

    @Test
    @DisplayName("Does not replace the death of an enchanted creature controlled by an opponent")
    void doesNotReplaceOpponentControlledCreature() {
        Permanent dyingCreature = addCreature(player2);
        attach(new NecromancersMagemark(), dyingCreature, player1);
        Card dyingCard = dyingCreature.getCard();

        destroyWithDoomBlade(dyingCreature);

        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(card -> card.getId().equals(dyingCard.getId()));
        assertThat(gd.playerHands.get(player2.getId())).noneMatch(card -> card.getId().equals(dyingCard.getId()));
    }

    private Permanent addCreature(Player controller) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(controller.getId()).add(creature);
        return creature;
    }

    private void attach(Card auraCard, Permanent creature, Player controller) {
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }

    private void destroyWithDoomBlade(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }
}
