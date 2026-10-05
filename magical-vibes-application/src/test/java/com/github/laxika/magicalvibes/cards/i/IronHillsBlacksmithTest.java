package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IronHillsBlacksmith.class, GrizzlyBears.class})
class IronHillsBlacksmithTest extends BaseCardTest {

    @Test
    void entersAndCreatesAnAxeThatCanEquipAndBoostACreature() {
        harness.setHand(player1, List.of(new IronHillsBlacksmith()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent axe = findPermanent(player1, "Axe");
        assertThat(axe.getCard().getSubtypes()).containsExactly(CardSubtype.EQUIPMENT);

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int axeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(axe);
        harness.activateAbility(player1, axeIndex, null, creature.getId());
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void axeEntersUnattachedAndDoesNotBoostTheBlacksmithAutomatically() {
        Permanent blacksmith = castBlacksmithAndCreateAxe();
        Permanent axe = findPermanent(player1, "Axe");

        assertThat(countPermanents(player1, "Axe")).isEqualTo(1);
        assertThat(axe.getCard().isToken()).isTrue();
        assertThat(axe.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(axe.getCard().getColors()).isEmpty();
        assertThat(axe.getCard().getSubtypes()).containsExactly(CardSubtype.EQUIPMENT);
        assertThat(axe.isTapped()).isFalse();
        assertThat(axe.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, blacksmith)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blacksmith)).isEqualTo(1);
    }

    @Test
    void reequippingMovesTheBonusToTheNewCreature() {
        Permanent first = castBlacksmithAndCreateAxe();
        Permanent second = harness.addToBattlefieldAndReturn(player1, new IronHillsBlacksmith());
        Permanent axe = findPermanent(player1, "Axe");
        int axeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(axe);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, axeIndex, null, first.getId());
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, axeIndex, null, second.getId());
        resolveAllTriggers();

        assertThat(axe.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(1);
    }

    @Test
    void axeCannotEquipAnOpponentsCreature() {
        castBlacksmithAndCreateAxe();
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new IronHillsBlacksmith());
        Permanent axe = findPermanent(player1, "Axe");
        int axeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(axe);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, axeIndex, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(axe.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(1);
    }

    @Test
    void axeRequiresTwoManaToEquip() {
        Permanent blacksmith = castBlacksmithAndCreateAxe();
        Permanent axe = findPermanent(player1, "Axe");
        int axeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(axe);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, axeIndex, null, blacksmith.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(axe.getAttachedTo()).isNull();
    }

    @Test
    void equippedBlacksmithDealsBoostedDamageInBothCombatDamageSteps() {
        Permanent blacksmith = castBlacksmithAndCreateAxe();
        Permanent axe = findPermanent(player1, "Axe");
        int axeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(axe);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, axeIndex, null, blacksmith.getId());
        resolveAllTriggers();
        blacksmith.setSummoningSick(false);
        harness.setLife(player2, 20);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(blacksmith)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private Permanent castBlacksmithAndCreateAxe() {
        harness.setHand(player1, List.of(new IronHillsBlacksmith()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Iron Hills Blacksmith");
    }
}
