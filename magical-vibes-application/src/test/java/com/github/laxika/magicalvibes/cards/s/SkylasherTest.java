package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
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

class SkylasherTest extends BaseCardTest {

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

    private static Card createBlueInstant() {
        Card card = new Card();
        card.setName("Blue Zap");
        card.setType(CardType.INSTANT);
        card.setManaCost("{U}");
        card.setColor(CardColor.BLUE);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }

    private Permanent addSkylasher(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = new Permanent(new Skylasher());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    @Test
    @DisplayName("Skylasher cannot be countered by Cancel")
    void cannotBeCountered() {
        Skylasher skylasher = new Skylasher();
        harness.setHand(player1, List.of(skylasher));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, skylasher.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skylasher");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Blue spell cannot target Skylasher")
    void blueSpellCannotTarget() {
        Permanent skylasher = addSkylasher(player2);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(createBlueInstant()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, skylasher.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("Green spell can target Skylasher")
    void greenSpellCanTarget() {
        Permanent skylasher = addSkylasher(player1);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, skylasher.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Giant Growth");
    }

    @Test
    @DisplayName("Blue creature cannot block Skylasher")
    void blueCreatureCannotBlock() {
        Permanent attacker = addSkylasher(player1);
        attacker.setAttacking(true);

        Permanent blocker = new Permanent(createCreature("Merfolk Scout", 2, 2, CardColor.BLUE));
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
    @DisplayName("Skylasher takes no combat damage from a blue creature")
    void takesNoDamageFromBlueCreature() {
        Permanent attacker = new Permanent(createCreature("Sea Serpent", 5, 5, CardColor.BLUE));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = addSkylasher(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Skylasher");
    }

    @Test
    @DisplayName("Skylasher dies to combat damage from a non-blue creature")
    void diesToNonBlueCreature() {
        Permanent attacker = new Permanent(createCreature("Big Green", 5, 5, CardColor.GREEN));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = addSkylasher(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Skylasher");
    }
}
