package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronGiant;
import com.github.laxika.magicalvibes.cards.v.Vizzerdrix;
import com.github.laxika.magicalvibes.cards.w.WorldChampionCelestialWeapon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SidequestPlayBlitzball.class, WorldChampionCelestialWeapon.class, GrizzlyBears.class, Vizzerdrix.class, IronGiant.class})
class SidequestPlayBlitzballTest extends BaseCardTest {

    @Test
    void beginningOfCombatBoostsTargetCreatureUntilEndOfTurn() {
        harness.addToBattlefield(player1, new SidequestPlayBlitzball());
        Permanent target = addReadyCreature(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    void transformsAfterAPlayerIsDealtSixCombatDamageAndAttachesToYourCreature() {
        Permanent source = addReadyPermanent(player1, new SidequestPlayBlitzball());
        Permanent attacker = addReadyCreature(player1, new Vizzerdrix());

        attacker.setAttacking(true);
        resolveCombat(player1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(attacker.getId()));

        assertThat(source.isTransformed()).isTrue();
        assertThat(source.getCard()).isInstanceOf(WorldChampionCelestialWeapon.class);
        assertThat(source.getAttachedTo()).isEqualTo(attacker.getId());
    }

    @Test
    void doesNotTransformWhenCombatDamageIsBelowSix() {
        Permanent source = addReadyPermanent(player1, new SidequestPlayBlitzball());
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        resolveCombat(player1);
        harness.passBothPriorities();

        assertThat(source.isTransformed()).isFalse();
        assertThat(source.getAttachedTo()).isNull();
    }

    @Test
    void doesNotTransformDuringAnOpponentsCombat() {
        Permanent source = addReadyPermanent(player1, new SidequestPlayBlitzball());
        Permanent attacker = addReadyCreature(player2, new Vizzerdrix());
        attacker.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(source.isTransformed()).isFalse();
    }

    @Test
    void transformedFaceBoostsEquippedCreatureAndGrantsDoubleStrike() {
        Permanent weapon = addTransformedWeapon(player1);
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        weapon.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void equipThreeAttachesWeaponToCreatureYouControl() {
        Permanent weapon = addTransformedWeapon(player1);
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(weapon.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void transformsEvenWhenNoCreatureRemainsToAttachTo() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SidequestPlayBlitzball());
        Permanent attacker = addReadyCreature(player1, new IronGiant());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);

        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            harness.passBothPriorities();
            harness.passBothPriorities();
        });

        assertThat(source.isTransformed()).isTrue();
        assertThat(source.getCard()).isInstanceOf(WorldChampionCelestialWeapon.class);
        assertThat(source.getAttachedTo()).isNull();
    }

    @Test
    void combinesCombatDamageFromMultipleCreaturesToOnePlayer() {
        Permanent source = addReadyPermanent(player1, new SidequestPlayBlitzball());
        Permanent first = addReadyCreature(player1, new GrizzlyBears());
        Permanent second = addReadyCreature(player1, new GrizzlyBears());
        Permanent third = addReadyCreature(player1, new GrizzlyBears());
        first.setAttacking(true);
        second.setAttacking(true);
        third.setAttacking(true);

        resolveCombat(player1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(source.isTransformed()).isTrue();
        assertThat(source.getAttachedTo()).isEqualTo(second.getId());
    }

    @Test
    void doesNotBoostCreaturesAtBeginningOfOpponentsCombat() {
        harness.addToBattlefield(player1, new SidequestPlayBlitzball());
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        return addReadyPermanent(player, card);
    }

    private Permanent addReadyPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addTransformedWeapon(Player player) {
        SidequestPlayBlitzball front = new SidequestPlayBlitzball();
        Permanent weapon = harness.addToBattlefieldAndReturn(player, front);
        weapon.setCard(front.getBackFaceCard());
        weapon.setTransformed(true);
        weapon.setSummoningSick(false);
        return weapon;
    }
}
