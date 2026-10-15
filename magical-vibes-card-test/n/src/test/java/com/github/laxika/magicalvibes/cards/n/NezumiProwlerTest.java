package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BronzeplateBoar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NezumiProwler.class, BronzeplateBoar.class})
class NezumiProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature you control deathtouch and lifelink until end of turn")
    void etbGrantsKeywordsToTargetCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BronzeplateBoar());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BronzeplateBoar());
        castProwler(target);

        assertThat(target.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(opponentCreature.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(opponentCreature.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("ETB-granted keywords wear off at end of turn")
    void etbGrantedKeywordsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BronzeplateBoar());
        castProwler(target);

        assertThat(target.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Ninjutsu returns an unblocked attacker and puts Nezumi Prowler onto the battlefield attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new BronzeplateBoar());
        addCreatureReady(player2, new BronzeplateBoar());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new NezumiProwler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Bronzeplate Boar");
        Permanent prowler = findPermanent(player1, "Nezumi Prowler");
        assertThat(prowler.isTapped()).isTrue();
        assertThat(prowler.isAttacking()).isTrue();
        assertThat(prowler.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB can target Nezumi Prowler itself and excludes opposing creatures")
    void etbCanTargetItself() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new BronzeplateBoar());
        harness.setHand(player1, List.of(new NezumiProwler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent prowler = findPermanent(player1, "Nezumi Prowler");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(prowler.getId())
                .doesNotContain(opponent.getId());
        harness.handlePermanentChosen(player1, prowler.getId());
        resolveAllTriggers();

        assertThat(prowler.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(prowler.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Ninjutsu pays the return cost immediately and triggers the keyword grant on entry")
    void ninjutsuTriggersKeywordGrant() {
        Permanent attacker = addCreatureReady(player1, new BronzeplateBoar());
        addCreatureReady(player2, new BronzeplateBoar());
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new NezumiProwler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, attacker.getId());

        harness.assertInHand(player1, "Bronzeplate Boar");
        harness.assertNotOnBattlefield(player1, "Bronzeplate Boar");
        harness.assertNotOnBattlefield(player1, "Nezumi Prowler");
        harness.passBothPriorities();

        Permanent prowler = findPermanent(player1, "Nezumi Prowler");
        harness.handlePermanentChosen(player1, prowler.getId());
        resolveAllTriggers();

        assertThat(prowler.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(prowler.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Ninjutsu cannot return an attacker before blockers have been declared")
    void ninjutsuCannotActivateBeforeBlockers() {
        Permanent attacker = addCreatureReady(player1, new BronzeplateBoar());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new NezumiProwler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Bronzeplate Boar");
        harness.assertInHand(player1, "Nezumi Prowler");
        assertThat(gd.stack).isEmpty();
    }

    private void castProwler(Permanent target) {
        harness.setHand(player1, List.of(new NezumiProwler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();
    }
}
