package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LivingDeath;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CanoptekTombSentinel.class, Forest.class, GrizzlyBears.class, LivingDeath.class})
class CanoptekTombSentinelTest extends BaseCardTest {

    @Test
    void unearthExilesUpToOneTargetNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new CanoptekTombSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void graveyardTriggerCannotTargetALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new CanoptekTombSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(creature.getId()).doesNotContain(land.getId());
    }

    @Test
    void castingFromHandDoesNotTriggerExileCannon() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CanoptekTombSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void unearthGrantsHasteAndExilesSentinelAtNextEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new CanoptekTombSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent sentinel = findPermanent(player1, "Canoptek Tomb Sentinel");
        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Canoptek Tomb Sentinel");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Canoptek Tomb Sentinel"));
    }

    @Test
    void exileCannonCanChooseNoTargetWhenSentinelIsTheOnlyNonlandPermanent() {
        harness.setGraveyard(player1, List.of(new CanoptekTombSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Canoptek Tomb Sentinel");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void exileCannonCanTargetTheSentinelItself() {
        harness.setGraveyard(player1, List.of(new CanoptekTombSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1,
                harness.getPermanentId(player1, "Canoptek Tomb Sentinel"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Canoptek Tomb Sentinel");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Canoptek Tomb Sentinel"));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void unearthCannotBeActivatedOutsideAMainPhase() {
        harness.setGraveyard(player1, List.of(new CanoptekTombSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertNotOnBattlefield(player1, "Canoptek Tomb Sentinel");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void unearthCannotBeActivatedWithOnlySixMana() {
        harness.setGraveyard(player1, List.of(new CanoptekTombSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertNotOnBattlefield(player1, "Canoptek Tomb Sentinel");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void unearthedSentinelIsExiledInsteadOfDying() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CanoptekTombSentinel());
        harness.setGraveyard(player1, List.of(new CanoptekTombSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent sentinel = findPermanent(player1, "Canoptek Tomb Sentinel");
        sentinel.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Canoptek Tomb Sentinel");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Canoptek Tomb Sentinel"));
    }

    @Test
    void livingDeathReturnsSentinelFromExileWithoutTriggeringExileCannon() {
        harness.setGraveyard(player1, List.of(new CanoptekTombSentinel()));
        harness.setHand(player1, List.of(new LivingDeath()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, List.of());

        harness.assertOnBattlefield(player1, "Canoptek Tomb Sentinel");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
