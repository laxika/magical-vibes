package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.l.LurkingLizards;
import com.github.laxika.magicalvibes.cards.z.ZuriWarriorOfWakanda;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnstableMoleculeSuit.class, ZuriWarriorOfWakanda.class, LurkingLizards.class})
class UnstableMoleculeSuitTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoostAndIndestructible() {
        Permanent creature = addCreatureReady(player1, new ZuriWarriorOfWakanda());
        Permanent suit = addSuitReady(player1);
        suit.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void commanderEquipCostsTwoAndAttachesToCommander() {
        Card commander = new ZuriWarriorOfWakanda();
        gd.makeCommander(player1.getId(), commander);
        Permanent commanderPermanent = addCreatureReady(player1, commander);
        Permanent suit = addSuitReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, commanderPermanent.getId());
        harness.passBothPriorities();

        assertThat(suit.getAttachedTo()).isEqualTo(commanderPermanent.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void regularEquipCostsFourAndAttachesToNonCommander() {
        Permanent creature = addCreatureReady(player1, new ZuriWarriorOfWakanda());
        Permanent suit = addSuitReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(suit.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void commanderEquipCannotTargetNonCommander() {
        Permanent creature = addCreatureReady(player1, new ZuriWarriorOfWakanda());
        addSuitReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void commanderEquipCanTargetOpponentsCommanderUnderYourControl() {
        Card commander = new ZuriWarriorOfWakanda();
        gd.makeCommander(player2.getId(), commander);
        Permanent creature = addCreatureReady(player1, commander);
        Permanent suit = addSuitReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(suit.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void neitherEquipAbilityCanTargetAnOpponentsCreature() {
        Card commander = new ZuriWarriorOfWakanda();
        gd.makeCommander(player2.getId(), commander);
        Permanent creature = addCreatureReady(player2, commander);
        addSuitReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void neitherEquipAbilityCanBeActivatedDuringCombat() {
        Card commander = new ZuriWarriorOfWakanda();
        gd.makeCommander(player1.getId(), commander);
        Permanent creature = addCreatureReady(player1, commander);
        addSuitReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void movingSuitTransfersBoostAndIndestructible() {
        Permanent first = addCreatureReady(player1, new ZuriWarriorOfWakanda());
        Permanent second = addCreatureReady(player1, new LurkingLizards());
        Permanent suit = addSuitReady(player1);
        suit.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 2, 1, null, second.getId());
        harness.passBothPriorities();

        assertThat(suit.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, second, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void equippedCreatureSurvivesLethalDamageButDiesAfterSuitLeaves() {
        Permanent creature = addCreatureReady(player1, new ZuriWarriorOfWakanda());
        Permanent suit = addSuitReady(player1);
        suit.setAttachedTo(creature.getId());
        creature.setMarkedDamage(4);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);

        gd.playerBattlefields.get(player1.getId()).remove(suit);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
    }

    private Permanent addSuitReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new UnstableMoleculeSuit());
    }
}
