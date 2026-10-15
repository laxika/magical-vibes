package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DaybreakRanger;
import com.github.laxika.magicalvibes.cards.p.PreyUpon;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FullMoonsRise.class, DaybreakRanger.class, WalkingCorpse.class, PreyUpon.class})
class FullMoonsRiseTest extends BaseCardTest {

    @Test
    @DisplayName("Werewolf creatures you control get +1/+0 and trample")
    void buffsWerewolvesYouControl() {
        harness.addToBattlefield(player1, new FullMoonsRise());
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new DaybreakRanger());

        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(3); // 2 base + 1 static
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(2); // 2 base + 0 static
        assertThat(gqs.hasKeyword(gd, ranger, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not buff non-Werewolf creatures")
    void doesNotBuffNonWerewolves() {
        harness.addToBattlefield(player1, new FullMoonsRise());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Does not buff opponent's Werewolf creatures")
    void doesNotBuffOpponentWerewolves() {
        harness.addToBattlefield(player1, new FullMoonsRise());
        Permanent opponentRanger = harness.addToBattlefieldAndReturn(player2, new DaybreakRanger());

        assertThat(gqs.getEffectivePower(gd, opponentRanger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentRanger)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentRanger, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Bonus is removed when Full Moon's Rise leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new FullMoonsRise());
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new DaybreakRanger());
        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ranger, Keyword.TRAMPLE)).isTrue();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ranger, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Activating sacrifice ability puts it on the stack and sacrifices Full Moon's Rise")
    void activationSacrificesAndStacksAbility() {
        harness.addToBattlefield(player1, new FullMoonsRise());
        harness.addToBattlefield(player1, new DaybreakRanger());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Full Moon's Rise");
        harness.assertInGraveyard(player1, "Full Moon's Rise");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Full Moon's Rise");
    }

    @Test
    @DisplayName("Resolving sacrifice ability gives regeneration shields to all Werewolves you control")
    void regeneratesAllWerewolves() {
        harness.addToBattlefield(player1, new FullMoonsRise());
        harness.addToBattlefield(player1, new DaybreakRanger());
        harness.addToBattlefield(player1, new DaybreakRanger());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Both werewolves should have a regeneration shield
        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Daybreak Ranger"))
                .forEach(p -> assertThat(p.getRegenerationShield()).isEqualTo(1));
    }

    @Test
    @DisplayName("Regeneration does not affect non-Werewolf creatures")
    void regenerationDoesNotAffectNonWerewolves() {
        harness.addToBattlefield(player1, new FullMoonsRise());
        harness.addToBattlefield(player1, new DaybreakRanger());
        harness.addToBattlefield(player1, new WalkingCorpse());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Walking Corpse");
        assertThat(bears.getRegenerationShield()).isZero();

        Permanent ranger = findPermanent(player1, "Daybreak Ranger");
        assertThat(ranger.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration does not affect opponent's Werewolf creatures")
    void regenerationDoesNotAffectOpponentWerewolves() {
        harness.addToBattlefield(player1, new FullMoonsRise());
        harness.addToBattlefield(player1, new DaybreakRanger());
        harness.addToBattlefield(player2, new DaybreakRanger());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent ownRanger = findPermanent(player1, "Daybreak Ranger");
        assertThat(ownRanger.getRegenerationShield()).isEqualTo(1);

        Permanent opponentRanger = findPermanent(player2, "Daybreak Ranger");
        assertThat(opponentRanger.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Regeneration shields prevent lethal fight damage and are consumed")
    void regenerationPreventsLethalFightDamage() {
        harness.addToBattlefield(player1, new FullMoonsRise());
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new DaybreakRanger());
        Permanent corpse = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ranger.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ranger, Keyword.TRAMPLE)).isFalse();

        harness.setHand(player1, List.of(new PreyUpon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, List.of(ranger.getId(), corpse.getId()));

        harness.assertOnBattlefield(player1, "Daybreak Ranger");
        harness.assertNotInGraveyard(player1, "Daybreak Ranger");
        harness.assertInGraveyard(player2, "Walking Corpse");
        assertThat(ranger.isTapped()).isTrue();
        assertThat(ranger.getMarkedDamage()).isZero();
        assertThat(ranger.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Werewolves present at resolution receive shields, later arrivals do not")
    void regenerationUsesCreaturesPresentAtResolution() {
        harness.addToBattlefield(player1, new FullMoonsRise());
        harness.activateAbility(player1, 0, null, null);

        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new DaybreakRanger());
        assertThat(beforeResolution.getRegenerationShield()).isZero();
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new DaybreakRanger());

        assertThat(beforeResolution.getRegenerationShield()).isEqualTo(1);
        assertThat(afterResolution.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Transformed Werewolves receive the bonus and regeneration shield")
    void affectsTransformedWerewolves() {
        harness.addToBattlefield(player1, new FullMoonsRise());
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new DaybreakRanger());

        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(ranger.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ranger, Keyword.TRAMPLE)).isTrue();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ranger.getRegenerationShield()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ranger, Keyword.TRAMPLE)).isFalse();
    }
}
