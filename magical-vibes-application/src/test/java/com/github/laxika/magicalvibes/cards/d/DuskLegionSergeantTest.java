package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireNeonate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuskLegionSergeant.class, VampireNeonate.class, GrizzlyBears.class})
class DuskLegionSergeantTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing it grants temporary persist to nontoken Vampires you control")
    void grantsPersistToNontokenVampiresUntilEndOfTurn() {
        Permanent sergeant = addCreatureReady(player1, new DuskLegionSergeant());
        Permanent vampire = addCreatureReady(player1, new VampireNeonate());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sergeant);
        assertThat(vampire.hasKeyword(Keyword.PERSIST)).isTrue();
        assertThat(bear.hasKeyword(Keyword.PERSIST)).isFalse();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(vampire.hasKeyword(Keyword.PERSIST)).isFalse();
    }

    @Test
    void excludesTokensAndOpponentsVampires() {
        addCreatureReady(player1, new DuskLegionSergeant());
        DuskLegionSergeant tokenCard = new DuskLegionSergeant();
        tokenCard.setToken(true);
        Permanent token = addCreatureReady(player1, tokenCard);
        Permanent opponent = addCreatureReady(player2, new DuskLegionSergeant());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(token);
        harness.assertInGraveyard(player1, "Dusk Legion Sergeant");
        harness.passBothPriorities();

        assertThat(token.hasKeyword(Keyword.PERSIST)).isFalse();
        assertThat(opponent.hasKeyword(Keyword.PERSIST)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void selectsVampiresAtResolutionButDoesNotAffectLaterEntrants() {
        addCreatureReady(player1, new DuskLegionSergeant());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player1, new DuskLegionSergeant());

        harness.passBothPriorities();
        Permanent afterResolution = harness.enterBattlefieldAndReturn(player1, new DuskLegionSergeant());

        assertThat(beforeResolution.hasKeyword(Keyword.PERSIST)).isTrue();
        assertThat(afterResolution.hasKeyword(Keyword.PERSIST)).isFalse();
    }

    @Test
    void grantedPersistReturnsVampireWithCounterAndDoesNotFollowNewObject() {
        addCreatureReady(player1, new DuskLegionSergeant());
        Permanent vampire = addCreatureReady(player1, new DuskLegionSergeant());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        vampire.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Dusk Legion Sergeant");
        assertThat(returned).isNotNull();
        assertThat(returned.getId()).isNotEqualTo(vampire.getId());
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(returned.hasKeyword(Keyword.PERSIST)).isFalse();
        returned.setMarkedDamage(1);
        harness.runStateBasedActions();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Dusk Legion Sergeant");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void vampireWithExistingMinusCounterDoesNotPersist() {
        addCreatureReady(player1, new DuskLegionSergeant());
        Permanent vampire = addCreatureReady(player1, new DuskLegionSergeant());
        vampire.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(vampire.hasKeyword(Keyword.PERSIST)).isTrue();

        vampire.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Dusk Legion Sergeant");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void multipleGrantsTriggerSeparatelyButReturnVampireOnlyOnce() {
        addCreatureReady(player1, new DuskLegionSergeant());
        addCreatureReady(player1, new DuskLegionSergeant());
        Permanent vampire = addCreatureReady(player1, new DuskLegionSergeant());
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        vampire.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dusk Legion Sergeant")).hasSize(1);
        assertThat(findPermanent(player1, "Dusk Legion Sergeant")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }
}
