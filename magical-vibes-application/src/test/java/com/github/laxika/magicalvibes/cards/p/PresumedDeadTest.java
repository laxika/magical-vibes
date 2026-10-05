package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PresumedDead.class, DoomBlade.class, GrizzlyBears.class})
class PresumedDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the target creature +2/+0 until end of turn")
    void boostsTargetCreature() {
        Permanent target = addCreature(player1);

        castPresumedDead(player1, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns the creature under its owner's control and suspects it when it dies")
    void returnsAndSuspectsCreatureWhenItDies() {
        Permanent target = addCreature(player2);
        Card targetCard = target.getCard();

        castPresumedDead(player1, target.getId());
        castDoomBlade(player1, target.getId());
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(targetCard.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(targetCard.getId()));
        assertThat(returned.isSuspected()).isTrue();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.MENACE)).isTrue();
        assertThat(bls.canBlock(gd, returned)).isFalse();
    }

    @Test
    @DisplayName("The death trigger expires at end of turn")
    void deathTriggerExpiresAtEndOfTurn() {
        Permanent target = addCreature(player1);
        Card targetCard = target.getCard();

        castPresumedDead(player1, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        castDoomBlade(player2, target.getId());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(targetCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(targetCard.getId()));
    }

    @Test
    @DisplayName("The returned creature has no temporary boost or death ability")
    void returnedCreatureDoesNotKeepTemporaryEffects() {
        Permanent target = addCreature(player1);
        Card targetCard = target.getCard();

        castPresumedDead(player1, target.getId());
        castDoomBlade(player1, target.getId());
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(targetCard.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);

        castDoomBlade(player1, returned.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(targetCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(targetCard.getId()));
    }

    @Test
    @DisplayName("An older death trigger cannot return a creature that died again")
    void olderDeathTriggerCannotReturnNewGraveyardObject() {
        Permanent target = addCreature(player1);
        Card targetCard = target.getCard();

        castPresumedDead(player1, target.getId());
        castPresumedDead(player1, target.getId());
        castDoomBlade(player1, target.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(targetCard.getId()))
                .findFirst().orElseThrow();
        castDoomBlade(player1, returned.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(targetCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(targetCard.getId()));
    }

    @Test
    @DisplayName("The power boost expires at end of turn")
    void powerBoostExpiresAtEndOfTurn() {
        Permanent target = addCreature(player1);
        castPresumedDead(player1, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The returned creature stays suspected beyond end of turn")
    void suspectedDesignationDoesNotExpireAtEndOfTurn() {
        Permanent target = addCreature(player1);
        Card targetCard = target.getCard();
        castPresumedDead(player1, target.getId());
        castDoomBlade(player1, target.getId());
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(targetCard.getId()))
                .findFirst().orElseThrow();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(returned.isSuspected()).isTrue();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.MENACE)).isTrue();
        assertThat(bls.canBlock(gd, returned)).isFalse();
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void castPresumedDead(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new PresumedDead()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(caster, 0, targetId);
    }

    private void castDoomBlade(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new DoomBlade()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
