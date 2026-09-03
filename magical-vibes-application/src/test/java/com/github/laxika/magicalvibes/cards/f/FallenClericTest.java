package com.github.laxika.magicalvibes.cards.f;

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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(FallenCleric.class)
class FallenClericTest extends BaseCardTest {

    @Test
    @DisplayName("Cleric creature cannot block Fallen Cleric")
    void clericCreatureCannotBlock() {
        Permanent fallenCleric = new Permanent(new FallenCleric());
        fallenCleric.setSummoningSick(false);
        fallenCleric.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(fallenCleric);

        Permanent blocker = new Permanent(createCreature("Cleric", CardSubtype.CLERIC, 2, 2));
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
    @DisplayName("Fallen Cleric takes no combat damage from a Cleric")
    void takesNoCombatDamageFromCleric() {
        Permanent attacker = new Permanent(createCreature("Cleric", CardSubtype.CLERIC, 3, 3));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent fallenCleric = new Permanent(new FallenCleric());
        fallenCleric.setSummoningSick(false);
        fallenCleric.setBlocking(true);
        fallenCleric.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(fallenCleric);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cleric spell cannot target Fallen Cleric")
    void clericSpellCannotTarget() {
        Permanent fallenCleric = new Permanent(new FallenCleric());
        fallenCleric.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(fallenCleric);

        Permanent validTarget = new Permanent(createCreature("Zombie", CardSubtype.ZOMBIE, 2, 2));
        validTarget.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(validTarget);

        Card spell = new Card();
        spell.setName("Cleric Bolt");
        spell.setType(CardType.INSTANT);
        spell.setManaCost("{W}");
        spell.setColor(CardColor.WHITE);
        spell.setSubtypes(List.of(CardSubtype.CLERIC));
        spell.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, fallenCleric.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Can be cast face down and turned face up for its morph cost")
    void canBeCastFaceDownAndTurnedFaceUp() {
        harness.setHand(player1, List.of(new FallenCleric()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent fallenCleric = findPermanent(player1, "Fallen Cleric");
        assertThat(fallenCleric.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(fallenCleric));
        harness.passBothPriorities();

        assertThat(fallenCleric.isFaceDown()).isFalse();
    }

    private static Card createCreature(String name, CardSubtype subtype, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{3}");
        card.setColor(CardColor.WHITE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
