package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CleverConjurer;
import com.github.laxika.magicalvibes.cards.g.GnollHunter;
import com.github.laxika.magicalvibes.cards.p.PowerWordKill;
import com.github.laxika.magicalvibes.cards.t.TheBlackstaffOfWaterdeep;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DancingSword.class, PowerWordKill.class, GnollHunter.class, TheBlackstaffOfWaterdeep.class, CleverConjurer.class})
class DancingSwordTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GnollHunter());
        Permanent sword = attachSword(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, sword)).isFalse();
    }

    @Test
    @DisplayName("Declining the death trigger leaves the Equipment on the battlefield")
    void decliningTransformationLeavesEquipment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GnollHunter());
        Permanent sword = attachSword(player1, creature);

        destroyCreature(creature);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sword);
        assertThat(gqs.isCreature(gd, sword)).isFalse();
        assertThat(gqs.permanentHasSubtype(sword, CardSubtype.EQUIPMENT)).isTrue();
        assertThat(sword.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(sword.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Accepting the death trigger makes it a 2/1 Construct artifact with flying and ward")
    void acceptingTransformationMakesArtifactCreatureWithWard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GnollHunter());
        Permanent sword = attachSword(player1, creature);

        destroyCreature(creature);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sword)).isTrue();
        assertThat(gqs.isArtifact(gd, sword)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sword)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sword)).isEqualTo(1);
        assertThat(GameQueryService.permanentHasSubtype(sword, CardSubtype.CONSTRUCT)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(sword, CardSubtype.EQUIPMENT)).isFalse();
        assertThat(gqs.hasKeyword(gd, sword, Keyword.FLYING)).isTrue();
        assertThat(sword.getAttachedTo()).isNull();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new PowerWordKill()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, sword.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sword);
    }

    @Test
    @DisplayName("Equip pays one mana and can move the Sword between creatures")
    void equipMovesSwordBetweenCreatures() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new DancingSword());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GnollHunter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GnollHunter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        assertThat(sword.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();
        assertThat(sword.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("Paying ward allows an opponent's removal spell to destroy the transformed Sword")
    void payingWardAllowsRemoval() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GnollHunter());
        Permanent sword = attachSword(player1, creature);
        destroyCreature(creature);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new PowerWordKill()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, sword.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sword);
        harness.assertInGraveyard(player1, "Dancing Sword");
    }

    @Test
    @DisplayName("The controller's own spell does not trigger ward")
    void ownSpellDoesNotTriggerWard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GnollHunter());
        Permanent sword = attachSword(player1, creature);
        destroyCreature(creature);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new PowerWordKill()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, sword.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sword);
        harness.assertInGraveyard(player1, "Dancing Sword");
    }

    @Test
    @DisplayName("Animation by another card does not grant the Sword ward")
    void externalAnimationDoesNotGrantWard() {
        harness.addToBattlefield(player1, new TheBlackstaffOfWaterdeep());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new DancingSword());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, sword.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, sword)).isTrue();

        harness.setHand(player2, List.of(new PowerWordKill()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, sword.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sword);
        harness.assertInGraveyard(player1, "Dancing Sword");
    }

    @Test
    @DisplayName("Declining transformation permits equipping the Sword to another creature")
    void declinedTransformationCanBeEquippedAgain() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GnollHunter());
        Permanent sword = attachSword(player1, creature);
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new GnollHunter());
        destroyCreature(creature);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, replacement.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(replacement.getId());
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, replacement)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, sword)).isFalse();
    }

    @Test
    @DisplayName("Ward counters an opponent's activated ability when payment is declined")
    void wardCountersOpponentAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GnollHunter());
        Permanent sword = attachSword(player1, creature);
        Permanent conjurer = harness.addToBattlefieldAndReturn(player2, new CleverConjurer());
        conjurer.setSummoningSick(false);
        destroyCreature(creature);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        sword.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, sword.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(sword.isTapped()).isTrue();
        assertThat(conjurer.isTapped()).isTrue();
    }

    private Permanent attachSword(Player player, Permanent creature) {
        Permanent sword = harness.addToBattlefieldAndReturn(player, new DancingSword());
        sword.setAttachedTo(creature.getId());
        return sword;
    }

    private void destroyCreature(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new PowerWordKill()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, creature.getId());
    }
}
