package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VenomousWords;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScathingShadelockVenomousWords.class, VenomousWords.class, GrizzlyBears.class})
class ScathingShadelockVenomousWordsTest extends BaseCardTest {

    @Test
    @DisplayName("Scathing Shadelock becomes prepared at the beginning of its controller's precombat main phase")
    void becomesPreparedAtPrecombatMain() {
        Permanent shadelock = addCreatureReady(player1, new ScathingShadelockVenomousWords());

        advanceToPrecombatMain(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(shadelock.isPrepared()).isTrue();
        UUID copyId = shadelock.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Casting Venomous Words unprepares Scathing Shadelock and buffs a creature you control")
    void castingVenomousWordsBuffsControlledCreature() {
        Permanent shadelock = addCreatureReady(player1, new ScathingShadelockVenomousWords());
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        UUID copyId = shadelock.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, copyId, target.getId());
        harness.passBothPriorities();

        assertThat(shadelock.isPrepared()).isFalse();
        assertThat(shadelock.getPreparedSpellCardId()).isNull();
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gd.findExiledCard(copyId)).isNull();
    }

    @Test
    @DisplayName("Venomous Words cannot target a creature an opponent controls")
    void cannotTargetOpponentsCreature() {
        Permanent shadelock = addCreatureReady(player1, new ScathingShadelockVenomousWords());
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        UUID copyId = shadelock.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Venomous Words's boost and deathtouch wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent shadelock = addCreatureReady(player1, new ScathingShadelockVenomousWords());
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, shadelock.getPreparedSpellCardId(), target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("An opponent's first main phase does not prepare Scathing Shadelock")
    void opponentsMainPhaseDoesNotPrepare() {
        Permanent shadelock = addCreatureReady(player1, new ScathingShadelockVenomousWords());

        advanceToPrecombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(shadelock.isPrepared()).isFalse();
        assertThat(shadelock.getPreparedSpellCardId()).isNull();
    }

    @Test
    @DisplayName("Becoming prepared again does not create another copy while already prepared")
    void alreadyPreparedKeepsSameCopy() {
        Permanent shadelock = addCreatureReady(player1, new ScathingShadelockVenomousWords());
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        UUID copyId = shadelock.getPreparedSpellCardId();
        int exileCount = gd.exiledCards.size();

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(shadelock.isPrepared()).isTrue();
        assertThat(shadelock.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.exiledCards).hasSize(exileCount);
    }

    @Test
    @DisplayName("Casting Venomous Words unprepares the source before the spell resolves")
    void unpreparesDuringCastingAndCanPrepareAgain() {
        Permanent shadelock = addCreatureReady(player1, new ScathingShadelockVenomousWords());
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        UUID firstCopyId = shadelock.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromExile(player1, firstCopyId, shadelock.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(shadelock.isPrepared()).isFalse();
        assertThat(shadelock.getPreparedSpellCardId()).isNull();
        assertThat(gqs.hasKeyword(gd, shadelock, Keyword.DEATHTOUCH)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, shadelock, Keyword.DEATHTOUCH)).isTrue();

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(shadelock.isPrepared()).isTrue();
        assertThat(shadelock.getPreparedSpellCardId()).isNotNull().isNotEqualTo(firstCopyId);
        assertThat(gd.findExiledCard(shadelock.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    @DisplayName("Venomous Words cannot be cast during combat and a rejected cast leaves the source prepared")
    void sorceryTimingIsRequired() {
        Permanent shadelock = addCreatureReady(player1, new ScathingShadelockVenomousWords());
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        UUID copyId = shadelock.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId, shadelock.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(shadelock.isPrepared()).isTrue();
        assertThat(shadelock.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Venomous Words does not affect a target that changes controller before resolution")
    void targetMustRemainControlledAtResolution() {
        Permanent shadelock = addCreatureReady(player1, new ScathingShadelockVenomousWords());
        Permanent target = addCreatureReady(player1, new ScathingShadelockVenomousWords());
        advanceToPrecombatMain(player1);
        resolveAllTriggers();
        int initialPower = target.getEffectivePower();
        int initialToughness = target.getEffectiveToughness();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, shadelock.getPreparedSpellCardId(), target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(initialPower);
        assertThat(target.getEffectiveToughness()).isEqualTo(initialToughness);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
        assertThat(shadelock.isPrepared()).isFalse();
    }

    private void advanceToPrecombatMain(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
