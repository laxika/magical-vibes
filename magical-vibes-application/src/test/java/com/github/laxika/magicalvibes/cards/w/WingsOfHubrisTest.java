package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FlickerOfFate;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WingsOfHubris.class, NyxbornColossus.class, FlickerOfFate.class})
class WingsOfHubrisTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has flying")
    void equippedCreatureHasFlying() {
        Permanent creature = addCreatureReady(player1, new NyxbornColossus());
        Permanent wings = addWingsReady(player1);
        wings.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Sacrifice ability makes the equipped creature unable to be blocked and sacrifices the Wings")
    void sacrificeAbilityMakesEquippedCreatureUnblockable() {
        Permanent creature = addCreatureReady(player1, new NyxbornColossus());
        Permanent wings = addWingsReady(player1);
        wings.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.isCantBeBlocked()).isTrue();
        harness.assertInGraveyard(player1, "Wings of Hubris");
        harness.assertNotOnBattlefield(player1, "Wings of Hubris");
    }

    @Test
    @DisplayName("Equipped creature is sacrificed at the beginning of the next end step")
    void equippedCreatureIsSacrificedAtNextEndStep() {
        Permanent creature = addCreatureReady(player1, new NyxbornColossus());
        Permanent wings = addWingsReady(player1);
        wings.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nyxborn Colossus");
        harness.assertInGraveyard(player1, "Nyxborn Colossus");
    }

    @Test
    @DisplayName("The equipped creature is not sacrificed if its controller is an opponent")
    void doesNotSacrificeCreatureControlledByOpponent() {
        Permanent creature = addCreatureReady(player2, new NyxbornColossus());
        Permanent wings = addWingsReady(player1);
        wings.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player2, "Nyxborn Colossus");
    }

    @Test
    @DisplayName("Equip costs one mana and moving the Wings moves flying")
    void equipMovesFlyingToNewCreature() {
        Permanent first = addCreatureReady(player1, new NyxbornColossus());
        Permanent second = addCreatureReady(player1, new NyxbornColossus());
        Permanent wings = addWingsReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 2, 1, null, first.getId());
        harness.passBothPriorities();

        assertThat(wings.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isFalse();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 2, 1, null, second.getId());
        harness.passBothPriorities();

        assertThat(wings.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipCannotTargetOpponentCreature() {
        Permanent creature = addCreatureReady(player2, new NyxbornColossus());
        addWingsReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void equipRequiresSorceryTiming() {
        Permanent creature = addCreatureReady(player1, new NyxbornColossus());
        addWingsReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unattached Wings can be sacrificed without affecting any creature")
    void unattachedWingsCanBeSacrificed() {
        Permanent creature = addCreatureReady(player1, new NyxbornColossus());
        addWingsReady(player1);

        harness.activateAbility(player1, 1, null, null);
        harness.assertInGraveyard(player1, "Wings of Hubris");
        harness.passBothPriorities();

        assertThat(creature.isCantBeBlocked()).isFalse();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Nyxborn Colossus");
    }

    @Test
    @DisplayName("Sacrificing the Wings removes flying immediately, before unblockability resolves")
    void sacrificeIsPaidBeforeAbilityResolves() {
        Permanent creature = addCreatureReady(player1, new NyxbornColossus());
        Permanent wings = addWingsReady(player1);
        wings.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 1, null, null);

        harness.assertInGraveyard(player1, "Wings of Hubris");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(creature.isCantBeBlocked()).isFalse();

        harness.passBothPriorities();

        assertThat(creature.isCantBeBlocked()).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Blinking the creature after resolution avoids the delayed sacrifice")
    void blinkedCreatureIsNotSacrificed() {
        Permanent creature = addCreatureReady(player1, new NyxbornColossus());
        Permanent wings = addWingsReady(player1);
        wings.setAttachedTo(creature.getId());
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(creature.isCantBeBlocked()).isTrue();

        harness.setHand(player1, List.of(new FlickerOfFate()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Nyxborn Colossus");
        assertThat(returned.getId()).isNotEqualTo(creature.getId());
        assertThat(returned.isCantBeBlocked()).isFalse();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nyxborn Colossus");
        harness.assertNotInGraveyard(player1, "Nyxborn Colossus");
    }

    @Test
    @DisplayName("The creature is sacrificed at the next end step even on an opponent's turn")
    void sacrificeOccursAtOpponentsEndStep() {
        Permanent creature = addCreatureReady(player1, new NyxbornColossus());
        Permanent wings = addWingsReady(player1);
        wings.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nyxborn Colossus");
        harness.assertInGraveyard(player1, "Nyxborn Colossus");
    }

    private Permanent addWingsReady(Player player) {
        return addCreatureReady(player, new WingsOfHubris());
    }
}
