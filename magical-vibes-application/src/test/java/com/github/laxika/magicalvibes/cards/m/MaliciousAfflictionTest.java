package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CarrionFeeder;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaliciousAffliction.class, LlanowarElves.class, CarrionFeeder.class})
class MaliciousAfflictionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target nonblack creature without morbid")
    void destroysTargetWithoutMorbid() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Morbid offers a copy that can be retargeted")
    void morbidCopiesAndRetargetsSpell() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        harness.setHand(player1, List.of(new MaliciousAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, firstTarget.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, secondTarget.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CarrionFeeder());
        harness.setHand(player1, List.of(new MaliciousAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The controller can decline the morbid copy")
    void mayDeclineCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        dyingCreature.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.setHand(player1, List.of(new MaliciousAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(survivor);
        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A morbid copy may keep the original target without casting another spell")
    void copyMayKeepOriginalTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        harness.setHand(player1, List.of(new MaliciousAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(survivor);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(survivor);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof MaliciousAffliction).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A death after casting does not enable the morbid trigger")
    void deathAfterCastingDoesNotCreateCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        harness.setHand(player1, List.of(new MaliciousAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
        dyingCreature.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The copy cannot be retargeted to a black creature")
    void copyCannotTargetBlackCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new CarrionFeeder());
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);

        harness.setHand(player1, List.of(new MaliciousAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, blackCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(blackCreature);
        assertThat(gd.stack).isEmpty();
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new MaliciousAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
