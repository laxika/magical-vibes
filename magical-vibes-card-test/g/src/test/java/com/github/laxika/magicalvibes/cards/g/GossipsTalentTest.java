package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.cards.t.Thrummingbird;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GossipsTalent.class, BarkformHarvester.class, Thrummingbird.class})
class GossipsTalentTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 1 when a creature you control enters")
    void surveilsWhenCreatureEnters() {
        castTalent();
        Card topCard = new BarkformHarvester();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new BarkformHarvester()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("At level 2, an attacking creature with power 3 or less cannot be blocked")
    void levelTwoMakesTargetAttackerUnblockable() {
        Permanent talent = castTalent();
        levelUp(talent, 0, 1);
        Permanent attacker = addCreatureReady(player1, new BarkformHarvester());

        declareAttackers(List.of(1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(attacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();

        assertThat(attacker.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Level 2 attack ability does not trigger before the Class reaches level 2")
    void levelTwoAbilityIsInactiveAtLevelOne() {
        castTalent();
        addCreatureReady(player1, new BarkformHarvester());

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("At level 3, combat damage may flicker the dealing creature")
    void levelThreeMayFlickerCombatDamagingCreature() {
        Permanent talent = castTalent();
        levelUp(talent, 0, 1);
        levelUp(talent, 1, 3);
        Permanent attacker = addCreatureReady(player1, new BarkformHarvester());
        UUID oldId = attacker.getId();

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        Permanent returned = findPermanent(player1, "Barkform Harvester");
        assertThat(returned.getId()).isNotEqualTo(oldId);
    }

    @Test
    @DisplayName("Surveil may leave the top card in the library")
    void mayKeepSurveilledCard() {
        castTalent();
        Card topCard = new BarkformHarvester();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new BarkformHarvester()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's creature entering does not trigger surveil")
    void opponentCreatureDoesNotTriggerSurveil() {
        castTalent();
        Card topCard = new BarkformHarvester();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BarkformHarvester()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Level 2 can target power 3 but excludes power 4 and nonattacking creatures")
    void attackTargetMustBeAttackingWithPowerAtMostThree() {
        Permanent talent = castTalent();
        levelUp(talent, 0, 1);
        Permanent eligible = addCreatureReady(player1, new BarkformHarvester());
        eligible.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent tooLarge = addCreatureReady(player1, new BarkformHarvester());
        tooLarge.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addCreatureReady(player1, new BarkformHarvester());

        declareAttackers(List.of(1, 2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(eligible.getId());
        harness.handlePermanentChosen(player1, eligible.getId());
        resolveAllTriggers();
        assertThat(eligible.isCantBeBlocked()).isTrue();
        assertThat(tooLarge.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The level 3 blink can be declined")
    void mayDeclineCombatDamageBlink() {
        Permanent talent = castTalent();
        levelUp(talent, 0, 1);
        levelUp(talent, 1, 3);
        Permanent attacker = addCreatureReady(player1, new BarkformHarvester());

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Barkform Harvester").getId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Combat damage at level 2 does not offer a blink")
    void levelTwoDoesNotBlink() {
        Permanent talent = castTalent();
        levelUp(talent, 0, 1);
        Permanent attacker = addCreatureReady(player1, new BarkformHarvester());

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Barkform Harvester").getId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Class levels do not make Gossip's Talent eligible for proliferate")
    void classLevelIsNotAProliferatableCounter() {
        Permanent talent = castTalent();
        levelUp(talent, 0, 1);
        Permanent creature = addCreatureReady(player1, new BarkformHarvester());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new Thrummingbird());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(creature.getId()).doesNotContain(talent.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(creature.getId()));
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Level 3 cannot be gained before level 2")
    void cannotSkipLevelTwo() {
        Permanent talent = castTalent();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(talent), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Class advancement is restricted to sorcery timing")
    void cannotLevelUpDuringCombat() {
        Permanent talent = castTalent();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(talent), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castTalent() {
        harness.setHand(player1, List.of(new GossipsTalent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Gossip's Talent");
    }

    private void levelUp(Permanent talent, int abilityIndex, int genericMana) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, genericMana);
        int talentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(talent);
        harness.activateAbility(player1, talentIndex, abilityIndex, null, null);
        resolveAllTriggers();
    }
}
