package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CruelUltimatum;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Sleep;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AishaOfSparksAndSmoke.class, Divination.class, GrizzlyBears.class, Shock.class, Sleep.class, CruelUltimatum.class})
class AishaOfSparksAndSmokeTest extends BaseCardTest {

    @Test
    @DisplayName("{R/W} grants first strike until end of turn")
    void firstStrikeGrantedAndWearsOff() {
        Permanent aisha = addCreatureReady(player1, new AishaOfSparksAndSmoke());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, aisha, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, aisha, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Combat damage offers a sorcery with mana value at most the damage dealt")
    void combatDamageOffersQualifyingSorcery() {
        Divination divination = new Divination();
        harness.setHand(player1, List.of(
                divination, new Shock(), new GrizzlyBears(), new CruelUltimatum()));
        attackAndResolveTrigger();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.description()).isEqualTo("Cast Divination without paying its mana cost?");

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(divination.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(divination.getId()));
    }

    @Test
    @DisplayName("Combat damage does not offer instants, creatures, or over-cap sorceries")
    void combatDamageOffersNoNonqualifyingCards() {
        harness.setHand(player1, List.of(
                new Shock(), new GrizzlyBears(), new CruelUltimatum()));
        attackAndResolveTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void combatDamageEqualToSorceryManaValueQualifies() {
        harness.setHand(player1, List.of(new Sleep()));
        attackAndResolveTrigger();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.description()).isEqualTo("Cast Sleep without paying its mana cost?");
    }

    @Test
    void damageToBlockerTriggersEvenWhenAishaDiesInCombat() {
        harness.setHand(player1, List.of(new Divination()));
        addCreatureReady(player1, new AishaOfSparksAndSmoke());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Aisha of Sparks and Smoke");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.description()).isEqualTo("Cast Divination without paying its mana cost?");
    }

    @Test
    void noncreatureSpellTriggersProwessUntilEndOfTurn() {
        Permanent aisha = addCreatureReady(player1, new AishaOfSparksAndSmoke());
        int originalPower = gqs.getEffectivePower(gd, aisha);
        int originalToughness = gqs.getEffectiveToughness(gd, aisha);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, aisha)).isEqualTo(originalPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, aisha)).isEqualTo(originalToughness + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aisha)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, aisha)).isEqualTo(originalToughness);
    }

    @Test
    void freeSorceryAlsoTriggersProwess() {
        Permanent aisha = addCreatureReady(player1, new AishaOfSparksAndSmoke());
        int originalPower = gqs.getEffectivePower(gd, aisha);
        int originalToughness = gqs.getEffectiveToughness(gd, aisha);
        harness.setHand(player1, List.of(new Divination()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, aisha)).isEqualTo(originalPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, aisha)).isEqualTo(originalToughness + 1);
        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void decliningFreeSorceryKeepsItInHandWithoutRevealingIt() {
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));
        attackAndResolveTrigger();
        int logSize = gd.gameLog.size();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Divination");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.subList(logSize, gd.gameLog.size()))
                .noneMatch(entry -> entry.plainText().contains("Divination"));
    }

    @Test
    void maySkipOneSorceryAndCastAnotherButOnlyOnePerTrigger() {
        Divination first = new Divination();
        Divination second = new Divination();
        Divination third = new Divination();
        harness.setHand(player1, List.of(first, second, third));
        attackAndResolveTrigger();

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(second.getId()));
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getId())
                .containsExactly(first.getId(), third.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void redManaCanPayForFirstStrike() {
        Permanent aisha = addCreatureReady(player1, new AishaOfSparksAndSmoke());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, aisha, Keyword.FIRST_STRIKE)).isTrue();
    }

    private void attackAndResolveTrigger() {
        addCreatureReady(player1, new AishaOfSparksAndSmoke());
        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
    }
}
