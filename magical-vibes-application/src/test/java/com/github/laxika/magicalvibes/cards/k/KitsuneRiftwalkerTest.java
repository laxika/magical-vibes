package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KitsuneRiftwalkerTest extends BaseCardTest {

    private static Card createCreature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(2);
        card.setToughness(2);
        if (subtype != null) {
            card.setSubtypes(List.of(subtype));
        }
        return card;
    }

    private static Card createInstant(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{R}");
        card.setColor(CardColor.RED);
        if (subtype != null) {
            card.setSubtypes(List.of(subtype));
        }
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }

    @Test
    @DisplayName("Spirit creature cannot block Kitsune Riftwalker")
    void spiritCreatureCannotBlock() {
        Permanent attacker = new Permanent(new KitsuneRiftwalker());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createCreature("Spirit", CardSubtype.SPIRIT));
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
    @DisplayName("Non-Spirit creature can block Kitsune Riftwalker")
    void nonSpiritCreatureCanBlock() {
        Permanent attacker = new Permanent(new KitsuneRiftwalker());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GrizzlyBears());
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
    @DisplayName("Arcane instant cannot target Kitsune Riftwalker")
    void arcaneInstantCannotTarget() {
        Permanent riftwalker = new Permanent(new KitsuneRiftwalker());
        riftwalker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(riftwalker);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(createInstant("Arcane Bolt", CardSubtype.ARCANE)));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, riftwalker.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Non-Arcane instant can target Kitsune Riftwalker")
    void nonArcaneInstantCanTarget() {
        Permanent riftwalker = new Permanent(new KitsuneRiftwalker());
        riftwalker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(riftwalker);

        harness.setHand(player1, List.of(createInstant("Regular Bolt", null)));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, riftwalker.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Regular Bolt");
    }
}
