package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SidequestPlayBlitzball.class, WorldChampionCelestialWeapon.class, GrizzlyBears.class, Vizzerdrix.class})
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
        Vizzerdrix attackerCard = new Vizzerdrix();
        attackerCard.setPower(6);
        attackerCard.setToughness(6);
        Permanent attacker = addReadyCreature(player1, attackerCard);

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
        Vizzerdrix attackerCard = new Vizzerdrix();
        attackerCard.setPower(3);
        attackerCard.setToughness(4);
        Permanent attacker = addReadyCreature(player1, attackerCard);

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
        Vizzerdrix attackerCard = new Vizzerdrix();
        attackerCard.setPower(6);
        attackerCard.setToughness(6);
        Permanent attacker = addReadyCreature(player2, attackerCard);
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

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addTransformedWeapon(Player player) {
        SidequestPlayBlitzball front = new SidequestPlayBlitzball();
        Permanent weapon = new Permanent(front);
        weapon.setCard(front.getBackFaceCard());
        weapon.setTransformed(true);
        weapon.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(weapon);
        return weapon;
    }
}
