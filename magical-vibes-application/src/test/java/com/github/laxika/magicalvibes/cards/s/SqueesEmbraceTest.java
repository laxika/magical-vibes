package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.u.UrborgElf;
import com.github.laxika.magicalvibes.cards.v.Vindicate;
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

@CardUsed({SqueesEmbrace.class, UrborgElf.class, Vindicate.class})
class SqueesEmbraceTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent creature = addCreatureWithAura(player1, player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("When enchanted creature dies, it returns to its owner's hand")
    void creatureReturnsToOwnerHandWhenDestroyed() {
        Permanent creature = addCreatureWithAura(player2, player1);
        Card creatureCard = creature.getCard();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Vindicate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, creature.getId());
        resolveAllTriggers();

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
        harness.setHand(player2, List.of(new Vindicate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        Permanent creature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof UrborgElf)
                .findFirst()
                .orElseThrow();
        harness.castSorcery(player2, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Squee's Embrace");
        harness.assertNotOnBattlefield(player1, "Squee's Embrace");
    }

    @Test
    @DisplayName("A different creature dying does not trigger the Aura")
    void differentCreatureDoesNotTriggerAura() {
        Permanent enchantedCreature = addCreatureWithAura(player1, player1);
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new UrborgElf());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Vindicate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castSorcery(player2, 0, otherCreature.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(otherCreature.getCard().getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(otherCreature.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(enchantedCreature.getId()));
    }

    private Permanent addCreatureWithAura(Player creatureController, Player auraController) {
        harness.addToBattlefield(creatureController, new UrborgElf());
        Permanent creature = gd.playerBattlefields.get(creatureController.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof UrborgElf)
                .findFirst()
                .orElseThrow();

        Permanent aura = new Permanent(new SqueesEmbrace());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);

        return creature;
    }
}
