package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.n.NettleSwine;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonsilverSpear.class, NettleSwine.class})
class MoonsilverSpearTest extends BaseCardTest {

    @Test
    @DisplayName("Equip {4} attaches the Equipment and grants first strike")
    void equipGrantsFirstStrike() {
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new MoonsilverSpear());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NettleSwine());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(spear.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Whenever equipped creature attacks, create a 4/4 white Angel with flying")
    void attackTriggerCreatesAngelToken() {
        Permanent creature = addCreatureReady(player1, new NettleSwine());
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new MoonsilverSpear());
        spear.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        Permanent angel = findPermanent(player1, "Angel");
        assertThat(angel.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(angel.getCard().getSubtypes()).contains(CardSubtype.ANGEL);
        assertThat(angel.getCard().getColors()).containsExactly(CardColor.WHITE);
        assertThat(angel.isTapped()).isFalse();
        assertThat(angel.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("No trigger when the Equipment is not attached")
    void noTriggerWhenUnattached() {
        addCreatureReady(player1, new NettleSwine());
        harness.addToBattlefieldAndReturn(player1, new MoonsilverSpear());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Moonsilver Spear"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angel"));
    }

    @Test
    void reequippingMovesFirstStrikeToTheNewCreature() {
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new MoonsilverSpear());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new NettleSwine());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new NettleSwine());
        spear.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(spear.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void attackTriggerStillCreatesAngelAfterSpearBecomesUnattached() {
        Permanent creature = addCreatureReady(player1, new NettleSwine());
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new MoonsilverSpear());
        spear.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        spear.setAttachedTo(null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Angel")).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void spearControllerCreatesTokenWhenOpponentsEquippedCreatureAttacks() {
        Permanent creature = addCreatureReady(player2, new NettleSwine());
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new MoonsilverSpear());
        spear.setAttachedTo(creature.getId());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Angel")).isEqualTo(1);
        assertThat(countPermanents(player2, "Angel")).isZero();
    }

    @Test
    void attackingWithAnotherCreatureDoesNotTriggerSpear() {
        Permanent equipped = addCreatureReady(player1, new NettleSwine());
        addCreatureReady(player1, new NettleSwine());
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new MoonsilverSpear());
        spear.setAttachedTo(equipped.getId());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Angel")).isZero();
    }
}
