package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.ReplicatingRing;
import com.github.laxika.magicalvibes.cards.s.SculptorOfWinter;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MammothGrowth.class, SculptorOfWinter.class, ReplicatingRing.class})
class MammothGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Gives +4/+4 to target creature until end of turn")
    void boostsTargetCreature() {
        harness.addToBattlefield(player1, new SculptorOfWinter());
        harness.setHand(player1, List.of(new MammothGrowth()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Sculptor of Winter");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(6);
        assertThat(bear.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new SculptorOfWinter());
        harness.setHand(player1, List.of(new MammothGrowth()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Sculptor of Winter");
        harness.castAndResolveInstant(player1, 0, bearId);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new ReplicatingRing());
        harness.setHand(player1, List.of(new MammothGrowth()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Replicating Ring");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Foretell exiles the card face down and allows casting it later")
    void foretellsAndCastsLater() {
        MammothGrowth spell = new MammothGrowth();
        harness.addToBattlefield(player2, new SculptorOfWinter());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(spell.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        UUID bearId = harness.getPermanentId(player2, "Sculptor of Winter");
        harness.castFromExile(player1, spell.getId(), bearId);
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(6);
        assertThat(bear.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    void cannotCastOnTheTurnItWasForetold() {
        MammothGrowth spell = new MammothGrowth();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SculptorOfWinter());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void cannotForetellDuringOpponentsTurn() {
        MammothGrowth spell = new MammothGrowth();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Foretell can only be used during your turn");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.findExiledCard(spell.getId())).isNull();
    }

    @Test
    void cannotForetellWithoutTwoMana() {
        MammothGrowth spell = new MammothGrowth();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Not enough mana to foretell");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.findExiledCard(spell.getId())).isNull();
    }

    @Test
    void removedTargetDoesNotBoostAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SculptorOfWinter());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SculptorOfWinter());
        harness.setHand(player1, List.of(new MammothGrowth()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Mammoth Growth");
    }
}
