package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CeruleanWyvernTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private static Card createTargetedInstant(String name, CardColor color, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }

    @Test
    @DisplayName("Green creature cannot block Cerulean Wyvern")
    void greenCreatureCannotBlock() {
        Permanent attacker = new Permanent(new CeruleanWyvern());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        // The Wyvern flies, so the green blocker gets flying too; protection from green is then what
        // stops the block.
        Card spider = createCreature("Giant Spider", 2, 4, CardColor.GREEN);
        spider.setKeywords(EnumSet.of(Keyword.FLYING));
        Permanent blocker = new Permanent(spider);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Non-green flyer can block Cerulean Wyvern")
    void nonGreenFlyerCanBlock() {
        Permanent attacker = new Permanent(new CeruleanWyvern());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Card whiteFlyer = createCreature("Cloud Elemental", 2, 2, CardColor.WHITE);
        whiteFlyer.setKeywords(EnumSet.of(Keyword.FLYING));
        Permanent blocker = new Permanent(whiteFlyer);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Takes no combat damage from a green creature")
    void takesNoDamageFromGreen() {
        Permanent attacker = new Permanent(createCreature("Craw Wurm", 6, 4, CardColor.GREEN));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new CeruleanWyvern());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Cerulean Wyvern");
    }

    @Test
    @DisplayName("Cannot be targeted by a green instant")
    void cannotBeTargetedByGreenInstant() {
        Permanent wyvern = new Permanent(new CeruleanWyvern());
        wyvern.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wyvern);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(createTargetedInstant("Wild Might", CardColor.GREEN, "{G}")));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, wyvern.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from green");
    }

    @Test
    @DisplayName("Can be targeted by a red instant")
    void canBeTargetedByRedInstant() {
        Permanent wyvern = new Permanent(new CeruleanWyvern());
        wyvern.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(wyvern);

        harness.setHand(player1, List.of(createTargetedInstant("Lightning Bolt", CardColor.RED, "{R}")));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, wyvern.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lightning Bolt");
    }
}
