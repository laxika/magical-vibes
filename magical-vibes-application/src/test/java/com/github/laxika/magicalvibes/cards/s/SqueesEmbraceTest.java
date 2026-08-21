package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SqueesEmbrace.class, GrizzlyBears.class, DoomBlade.class})
class SqueesEmbraceTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent creature = addCreatureWithAura(player1, player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("When enchanted creature dies, it returns to its owner's hand")
    void creatureReturnsToOwnerHandWhenDestroyed() {
        Permanent creature = addCreatureWithAura(player2, player1);
        Card creatureCard = creature.getCard();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(creatureCard.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("The Aura goes to its owner's graveyard when the enchanted creature dies")
    void auraGoesToOwnerGraveyardWhenCreatureDies() {
        addCreatureWithAura(player1, player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        Permanent creature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .findFirst()
                .orElseThrow();
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Squee's Embrace");
        harness.assertNotOnBattlefield(player1, "Squee's Embrace");
    }

    private Permanent addCreatureWithAura(Player creatureController, Player auraController) {
        harness.addToBattlefield(creatureController, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(creatureController.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .findFirst()
                .orElseThrow();

        Permanent aura = new Permanent(new SqueesEmbrace());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);

        return creature;
    }
}
