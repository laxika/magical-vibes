package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildHunger.class, DawntreaderElk.class, EvolvingWilds.class})
class WildHungerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Wild Hunger puts it on the stack")
    void castingPutsOnStack() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk()).getId();
        harness.setHand(player1, List.of(new WildHunger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Wild Hunger");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Wild Hunger gives +3/+1 and trample to target creature")
    void resolvingBoostsAndGrantsTrample() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk()).getId();
        harness.setHand(player1, List.of(new WildHunger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent elk = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(elk.getPowerModifier()).isEqualTo(3);
        assertThat(elk.getToughnessModifier()).isEqualTo(1);
        assertThat(elk.getEffectivePower()).isEqualTo(5);
        assertThat(elk.getEffectiveToughness()).isEqualTo(3);
        assertThat(elk.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Can target opponent's creature")
    void canTargetOpponentCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk()).getId();
        harness.setHand(player1, List.of(new WildHunger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent elk = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(elk.getPowerModifier()).isEqualTo(3);
        assertThat(elk.getToughnessModifier()).isEqualTo(1);
        assertThat(elk.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        UUID landId = harness.addToBattlefieldAndReturn(player1, new EvolvingWilds()).getId();
        harness.setHand(player1, List.of(new WildHunger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boost and trample wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk()).getId();
        harness.setHand(player1, List.of(new WildHunger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent elk = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(elk.getPowerModifier()).isEqualTo(0);
        assertThat(elk.getToughnessModifier()).isEqualTo(0);
        assertThat(elk.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Wild Hunger fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk()).getId();
        harness.setHand(player1, List.of(new WildHunger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, targetId);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Wild Hunger");
    }

    @Test
    @DisplayName("Goes to graveyard after resolving normally")
    void goesToGraveyardAfterResolving() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk()).getId();
        harness.setHand(player1, List.of(new WildHunger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wild Hunger");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flashback from graveyard gives +3/+1 and trample")
    void flashbackBoostsAndGrantsTrample() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk()).getId();
        harness.setGraveyard(player1, List.of(new WildHunger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent elk = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(elk.getPowerModifier()).isEqualTo(3);
        assertThat(elk.getToughnessModifier()).isEqualTo(1);
        assertThat(elk.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Flashback exiles the card after resolving")
    void flashbackExilesAfterResolving() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk()).getId();
        harness.setGraveyard(player1, List.of(new WildHunger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Wild Hunger");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Wild Hunger"));
    }

    @Test
    @DisplayName("Flashback puts instant spell on stack")
    void flashbackPutsOnStackAsInstant() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk()).getId();
        harness.setGraveyard(player1, List.of(new WildHunger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Wild Hunger");
        assertThat(gd.stack.getFirst().isCastWithFlashback()).isTrue();
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk()).getId();
        harness.setGraveyard(player1, List.of(new WildHunger()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback still exiles Wild Hunger when its target is sacrificed")
    void flashbackExilesWhenTargetBecomesIllegal() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk()).getId();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new WildHunger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFlashback(player1, 0, targetId);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Dawntreader Elk");
        harness.assertInGraveyard(player1, "Dawntreader Elk");
        harness.assertNotInGraveyard(player1, "Wild Hunger");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Wild Hunger"));
    }

    @Test
    @DisplayName("Flashback needs red mana even when the normal green cost is available")
    void flashbackCannotUseNormalManaCost() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk()).getId();
        harness.setGraveyard(player1, List.of(new WildHunger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Wild Hunger");
    }

    @Test
    @DisplayName("Casting then flashing back the same card stacks boosts for the turn")
    void normalCastAndFlashbackBoostsAccumulateAndExpire() {
        Permanent elk = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new WildHunger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, elk.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFlashback(player1, 0, elk.getId());
        harness.passBothPriorities();

        assertThat(elk.getEffectivePower()).isEqualTo(8);
        assertThat(elk.getEffectiveToughness()).isEqualTo(4);
        assertThat(elk.hasKeyword(Keyword.TRAMPLE)).isTrue();
        harness.assertNotInGraveyard(player1, "Wild Hunger");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Wild Hunger"));

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(elk.getPowerModifier()).isZero();
        assertThat(elk.getToughnessModifier()).isZero();
        assertThat(elk.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }
}
