package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SwordOfTheSqueak.class)
class SwordOfTheSqueakTest extends BaseCardTest {

    @Test
    void boostsEquippedCreatureForBasePowerOrToughnessOneCreaturesYouControl() {
        Permanent sword = addSwordReady(player1);
        Permanent equipped = addCreature(player1, "Equipped Creature", 2, 2, CardSubtype.BEAR);
        sword.setAttachedTo(equipped.getId());
        addCreature(player1, "Power One", 1, 3, CardSubtype.BEAR);
        addCreature(player1, "Toughness One", 2, 1, CardSubtype.BEAR);
        addCreature(player1, "Neither One", 2, 2, CardSubtype.BEAR);
        addCreature(player2, "Opponent Power One", 1, 1, CardSubtype.BEAR);

        assertThat(gqs.getEffectivePower(gd, equipped)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, equipped)).isEqualTo(4);
    }

    @Test
    void mayAttachToAQualifyingCreatureYouControlThatEnters() {
        Permanent sword = addSwordReady(player1);
        harness.setHand(player1, List.of(creature("Mouse", 2, 1, CardSubtype.MOUSE)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent mouse = findPermanent(player1, "Mouse");
        assertThat(sword.getAttachedTo()).isEqualTo(mouse.getId());
    }

    @Test
    void equipAttachesToTargetCreature() {
        Permanent sword = addSwordReady(player1);
        Permanent creature = addCreature(player1, "Creature", 2, 2, CardSubtype.BEAR);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addSwordReady(Player player) {
        Permanent sword = new Permanent(new SwordOfTheSqueak());
        sword.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(sword);
        return sword;
    }

    private Permanent addCreature(Player player, String name, int power, int toughness, CardSubtype subtype) {
        return harness.addToBattlefieldAndReturn(player, creature(name, power, toughness, subtype));
    }

    private static Card creature(String name, int power, int toughness, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
