package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.c.CrushingCanopy;
import com.github.laxika.magicalvibes.cards.d.DawnhartDisciple;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RadiantGrace.class, RadiantRestraints.class, DawnhartDisciple.class, Abrade.class, CrushingCanopy.class})
class RadiantGraceTest extends BaseCardTest {

    @Test
    void frontFaceBoostsAndGrantsVigilanceToEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RadiantGrace());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void enchantedCreatureDiesAndReturnsAsCurseAttachedToTargetOpponent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RadiantGrace());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, 0, creature.getId());
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, player2.getId());
        }
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(aura.getOriginalCard().getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCard()).isInstanceOf(RadiantRestraints.class);
        assertThat(returned.isTransformed()).isTrue();
        assertThat(returned.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    void backFaceMakesOnlyEnchantedPlayersCreaturesEnterTapped() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new RadiantGrace());
        curse.setCard(curse.getOriginalCard().getBackFaceCard());
        curse.setTransformed(true);
        curse.setAttachedTo(player2.getId());

        harness.setHand(player2, List.of(new DawnhartDisciple()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent opponentCreature = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof DawnhartDisciple)
                .findFirst()
                .orElseThrow();
        assertThat(opponentCreature.isTapped()).isTrue();

        harness.setHand(player1, List.of(new DawnhartDisciple()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent controllerCreature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof DawnhartDisciple)
                .findFirst()
                .orElseThrow();
        assertThat(controllerCreature.isTapped()).isFalse();
    }

    @Test
    void destroyingAuraDoesNotReturnItWhileEnchantedCreatureSurvives() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RadiantGrace());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new CrushingCanopy()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, 1, aura.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities();
        }

        harness.assertInGraveyard(player1, "Radiant Grace");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard().getId().equals(aura.getOriginalCard().getId()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void auraControllerGetsCurseWhenOpponentsEnchantedCreatureDies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DawnhartDisciple());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RadiantGrace());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 0, creature.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, player2.getId());
        }
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(aura.getOriginalCard().getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getCard()).isInstanceOf(RadiantRestraints.class);
        assertThat(returned.getAttachedTo()).isEqualTo(player2.getId());
        harness.assertInGraveyard(player2, "Dawnhart Disciple");
    }

    @Test
    void castingAuraOnOpponentsCreatureOnlyEnhancesThatCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new DawnhartDisciple());
        harness.setHand(player1, List.of(new RadiantGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isFalse();
    }
}
