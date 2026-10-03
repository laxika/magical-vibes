package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Persuasion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdventurousEaterHaveABite.class, GrizzlyBears.class, Persuasion.class})
class AdventurousEaterHaveABiteTest extends BaseCardTest {

    

    @Test
    @DisplayName("Entering the battlefield prepares Adventurous Eater and exiles a castable Have a Bite copy")
    void entersPrepared() {
        Permanent eater = castAdventurousEater();

        assertThat(eater.isPrepared()).isTrue();
        UUID copyId = eater.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(copyId);
    }

    @Test
    @DisplayName("Casting the prepared Have a Bite copy unprepares Adventurous Eater, adds a counter, and gains life")
    void castingPrepareCopyUnpreparesAndResolvesSpell() {
        Permanent eater = castAdventurousEater();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        UUID copyId = eater.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, copyId, target.getId());
        harness.passBothPriorities();

        assertThat(eater.isPrepared()).isFalse();
        assertThat(eater.getPreparedSpellCardId()).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    @Test
    @DisplayName("When prepared Adventurous Eater leaves the battlefield, the exiled copy ceases to exist")
    void leavingBattlefieldRemovesExiledCopy() {
        Permanent eater = castAdventurousEater();
        UUID copyId = eater.getPreparedSpellCardId();
        assertThat(gd.findExiledCard(copyId)).isNotNull();

        eater.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(eater);
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    private Permanent castAdventurousEater() {
        harness.setHand(player1, List.of(new AdventurousEaterHaveABite()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        resolveAllTriggers();

        return findPermanent(player1, "Adventurous Eater");
    }

    @Test
    @DisplayName("Adventurous Eater is prepared immediately when its creature spell resolves")
    void preparedBeforeAnyEnterTriggerResolves() {
        harness.setHand(player1, List.of(new AdventurousEaterHaveABite()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent eater = findPermanent(player1, "Adventurous Eater");
        assertThat(eater.isPrepared()).isTrue();
        assertThat(eater.getPreparedSpellCardId()).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Have a Bite can target an opposing creature but gains life for its caster")
    void opposingCreatureGetsCounterAndCasterGainsLife() {
        Permanent eater = castAdventurousEater();
        Permanent target = addCreatureReady(player2, new AdventurousEaterHaveABite());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, eater.getPreparedSpellCardId(), target.getId());
        assertThat(eater.isPrepared()).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Have a Bite gains no life when its only target dies before resolution")
    void illegalTargetPreventsLifeGainWithoutRepreparingSource() {
        Permanent eater = castAdventurousEater();
        Permanent target = addCreatureReady(player2, new AdventurousEaterHaveABite());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, eater.getPreparedSpellCardId(), target.getId());
        target.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(eater.isPrepared()).isFalse();
        assertThat(eater.getPreparedSpellCardId()).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The new controller of prepared Adventurous Eater may cast Have a Bite")
    void prepareSpellPermissionFollowsCreatureController() {
        Permanent eater = castAdventurousEater();
        UUID copyId = eater.getPreparedSpellCardId();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Persuasion()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player2, 0, eater.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(eater);
        assertThat(eater.isPrepared()).isTrue();
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castFromExile(player2, copyId, eater.getId());
        harness.passBothPriorities();

        assertThat(eater.isPrepared()).isFalse();
        assertThat(eater.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 21);
        harness.assertLife(player1, 20);
    }
}
