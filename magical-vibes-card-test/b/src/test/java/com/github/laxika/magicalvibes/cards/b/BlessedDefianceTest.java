package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CandlegroveWitch;
import com.github.laxika.magicalvibes.cards.f.FadingHope;
import com.github.laxika.magicalvibes.cards.p.PlayWithFire;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlessedDefiance.class, CandlegroveWitch.class, PlayWithFire.class, FadingHope.class})
class BlessedDefianceTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a creature +2/+0 and lifelink until end of turn")
    void buffsTargetUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.setHand(player1, List.of(new BlessedDefiance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Creates a Spirit when the targeted creature dies this turn")
    void createsSpiritWhenTargetDiesThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.setHand(player1, List.of(new BlessedDefiance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.setHand(player2, List.of(new PlayWithFire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Candlegrove Witch");
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not create a Spirit when the targeted creature survives")
    void doesNotCreateSpiritIfTargetSurvives() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.setHand(player1, List.of(new BlessedDefiance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CandlegroveWitch());
        harness.setHand(player1, List.of(new BlessedDefiance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void repeatedCastsCreateOneSpiritEach() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.setHand(player1, List.of(new BlessedDefiance(), new BlessedDefiance()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        harness.setHand(player2, List.of(new PlayWithFire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    void deathOnFollowingTurnDoesNotCreateSpirit() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.setHand(player1, List.of(new BlessedDefiance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.setHand(player2, List.of(new PlayWithFire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Candlegrove Witch");
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void targetDyingBeforeResolutionDoesNotCreateSpirit() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.setHand(player1, List.of(new BlessedDefiance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());

        harness.setHand(player2, List.of(new PlayWithFire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Candlegrove Witch");
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void returnedAndRecastCreatureIsNotTrackedByOldDeathTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.setHand(player1, List.of(new BlessedDefiance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new FadingHope(), new PlayWithFire()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        harness.assertInHand(player1, "Candlegrove Witch");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Candlegrove Witch");
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, returned.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Candlegrove Witch");
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void lifelinkGainsLifeFromBoostedCombatDamage() {
        Permanent target = addCreatureReady(player1, new CandlegroveWitch());
        harness.setHand(player1, List.of(new BlessedDefiance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castAndResolveInstant(player1, 0, target.getId());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }
}
