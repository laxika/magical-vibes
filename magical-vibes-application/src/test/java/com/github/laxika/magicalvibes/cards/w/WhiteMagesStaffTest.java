package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhiteMagesStaff.class, GrizzlyBears.class})
class WhiteMagesStaffTest extends BaseCardTest {

    @Test
    void enteringCreatesAndEquipsHero() {
        harness.setHand(player1, List.of(new WhiteMagesStaff()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent staff = findPermanent(player1, "White Mage's Staff");
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(staff.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero))
                .contains(CardSubtype.HERO, CardSubtype.CLERIC);
    }

    @Test
    void equippedCreatureGainsLifeWhenItAttacks() {
        Permanent staff = addStaffReady(player1);
        Permanent creature = addCreatureReady(player1);
        staff.setAttachedTo(creature.getId());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    void equipMovesStaffAndItsBonuses() {
        Permanent staff = addStaffReady(player1);
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        staff.setAttachedTo(first.getId());

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(staff.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, first)).doesNotContain(CardSubtype.CLERIC);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second)).contains(CardSubtype.CLERIC);
    }

    @Test
    void creaturesControllerGainsLifeEvenWhenOpponentControlsStaff() {
        Permanent staff = addStaffReady(player1);
        Permanent creature = addCreatureReady(player2);
        staff.setAttachedTo(creature.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void unattachedStaffDoesNotGrantAttackTrigger() {
        addStaffReady(player1);
        addCreatureReady(player1);
        harness.setLife(player1, 20);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void attackTriggerStillResolvesAfterStaffLeaves() {
        Permanent staff = addStaffReady(player1);
        Permanent creature = addCreatureReady(player1);
        staff.setAttachedTo(creature.getId());
        harness.setLife(player1, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(1)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(staff);
        gd.playerGraveyards.get(player1.getId()).add(staff.getCard());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature))
                .contains(CardSubtype.BEAR).doesNotContain(CardSubtype.CLERIC);
    }

    @Test
    void jobSelectStillCreatesHeroWhenStaffLeavesBeforeTriggerResolves() {
        harness.setHand(player1, List.of(new WhiteMagesStaff()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent staff = findPermanent(player1, "White Mage's Staff");
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(staff);
        gd.playerGraveyards.get(player1.getId()).add(staff.getCard());
        resolveAllTriggers();

        Permanent hero = findPermanent(player1, "Hero");
        assertThat(countPermanents(player1, "Hero")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero))
                .contains(CardSubtype.HERO).doesNotContain(CardSubtype.CLERIC);
    }

    private Permanent addStaffReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new WhiteMagesStaff());
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
