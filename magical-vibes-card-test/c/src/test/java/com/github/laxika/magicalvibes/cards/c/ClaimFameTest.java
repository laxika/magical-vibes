package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SpellSnare;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClaimFame.class, GrizzlyBears.class, SerraAngel.class, Forest.class})
class ClaimFameTest extends BaseCardTest {

    @Test
    @DisplayName("Claim returns a MV≤2 creature from your graveyard to the battlefield")
    void claimReanimatesLowManaValueCreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ClaimFame()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Claim");
    }

    @Test
    @DisplayName("Claim cannot target a creature with mana value greater than 2")
    void claimCannotTargetHighManaValueCreature() {
        Card angel = new SerraAngel();
        harness.setGraveyard(player1, List.of(angel));
        harness.setHand(player1, List.of(new ClaimFame()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, angel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Claim cannot target a card in an opponent's graveyard")
    void claimCannotTargetOpponentGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new ClaimFame()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Fame from graveyard gives +2/+0 and haste, then exiles")
    void fameFlashbackPumpsAndExiles() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new ClaimFame()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Claim");
        harness.assertNotInGraveyard(player1, "Fame");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Claim"));
    }

    @Test
    @DisplayName("Fame boost and haste wear off at end of turn")
    void fameEffectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new ClaimFame()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Fame cannot target a non-creature")
    void fameCannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setGraveyard(player1, List.of(new ClaimFame()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Fame requires sorcery timing")
    void fameRequiresSorceryTiming() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new ClaimFame()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
    }

    @Test
    void claimCannotTargetNonCreatureCard() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setHand(player1, List.of(new ClaimFame()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void claimDoesNotReturnTargetThatLeftGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ClaimFame()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        harness.assertInGraveyard(player1, "Claim");
    }

    @Test
    void fameCanTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new ClaimFame()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void fameIsExiledWhenItsOnlyTargetLeavesBattlefield() {
        Card creature = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player1, creature);
        Card spell = new ClaimFame();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFlashback(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.setGraveyard(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Claim");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    void claimThenFameUsesTheSamePhysicalCard() {
        Card creature = new GrizzlyBears();
        Card spell = new ClaimFame();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent target = findPermanent(player1, "Grizzly Bears");
        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Claim");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @CardUsed(SpellSnare.class)
    void fameIsAManaValueTwoSpellAndIsExiledWhenCountered() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card spell = new ClaimFame();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player2, List.of(new SpellSnare()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castFlashback(player1, 0, target.getId());
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        harness.assertNotInGraveyard(player1, "Claim");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }
}
