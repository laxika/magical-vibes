package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AethergeodeMiner;
import com.github.laxika.magicalvibes.cards.d.DruidOfTheCowl;
import com.github.laxika.magicalvibes.cards.i.ImplementOfCombustion;
import com.github.laxika.magicalvibes.cards.w.WelderAutomaton;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrackdownConstruct.class, AethergeodeMiner.class, DruidOfTheCowl.class,
        ImplementOfCombustion.class, WelderAutomaton.class})
class CrackdownConstructTest extends BaseCardTest {

    @Test
    @DisplayName("Activating artifact and creature abilities gives +1/+1 for each activation")
    void artifactAndCreatureAbilitiesBoostConstruct() {
        Permanent construct = addCreatureReady(player1, new CrackdownConstruct());
        addPermanentWithAbility(player1, CardType.ARTIFACT, new BoostSelfEffect(1, 0));
        addPermanentWithAbility(player1, CardType.CREATURE, new BoostSelfEffect(1, 0));

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        harness.activateAbility(player1, 2, null, null);
        resolveAllTriggers();

        assertThat(construct.getPowerModifier()).isEqualTo(2);
        assertThat(construct.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating a mana ability does not boost Construct")
    void manaAbilityDoesNotBoostConstruct() {
        Permanent construct = addCreatureReady(player1, new CrackdownConstruct());
        addPermanentWithAbility(player1, CardType.ARTIFACT, new AwardManaEffect(ManaColor.GREEN));

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(construct.getPowerModifier()).isZero();
        assertThat(construct.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Activating an enchantment ability does not boost Construct")
    void nonArtifactNonCreatureAbilityDoesNotBoostConstruct() {
        Permanent construct = addCreatureReady(player1, new CrackdownConstruct());
        addPermanentWithAbility(player1, CardType.ENCHANTMENT, new BoostSelfEffect(1, 0));

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(construct.getPowerModifier()).isZero();
        assertThat(construct.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An artifact creature activation triggers once and the boost expires at cleanup")
    void artifactCreatureTriggersOnceBeforeItsAbilityResolves() {
        Permanent construct = addCreatureReady(player1, new CrackdownConstruct());
        harness.addToBattlefield(player1, new WelderAutomaton());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(2);
        assertThat(construct.getPowerModifier()).isZero();
        harness.passBothPriorities();
        assertThat(construct.getPowerModifier()).isEqualTo(1);
        assertThat(construct.getToughnessModifier()).isEqualTo(1);
        harness.assertLife(player2, 20);

        resolveAllTriggers();
        harness.assertLife(player2, 19);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(construct.getPowerModifier()).isZero();
        assertThat(construct.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A nonartifact creature's energy ability triggers Construct")
    void nonartifactCreatureAbilityBoostsConstruct() {
        Permanent construct = addCreatureReady(player1, new CrackdownConstruct());
        harness.addToBattlefield(player1, new AethergeodeMiner());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(construct.getPowerModifier()).isEqualTo(1);
        assertThat(construct.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A sacrificed artifact still triggers Construct")
    void sacrificingAbilitySourceStillBoostsConstruct() {
        Permanent construct = addCreatureReady(player1, new CrackdownConstruct());
        harness.addToBattlefield(player1, new ImplementOfCombustion());
        harness.setLibrary(player1, List.of(new CrackdownConstruct()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.assertInGraveyard(player1, "Implement of Combustion");
        resolveAllTriggers();

        assertThat(construct.getPowerModifier()).isEqualTo(1);
        assertThat(construct.getToughnessModifier()).isEqualTo(1);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An opponent's artifact creature activation does not boost Construct")
    void opponentsActivationDoesNotBoostConstruct() {
        Permanent construct = addCreatureReady(player1, new CrackdownConstruct());
        harness.addToBattlefield(player2, new WelderAutomaton());
        harness.addMana(player2, ManaColor.RED, 4);

        harness.activateAbility(player2, 0, null, null);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(construct.getPowerModifier()).isZero();
        assertThat(construct.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A real creature's mana ability does not boost Construct")
    void creatureManaAbilityDoesNotBoostConstruct() {
        Permanent construct = addCreatureReady(player1, new CrackdownConstruct());
        addCreatureReady(player1, new DruidOfTheCowl());

        harness.tapPermanent(player1, 1);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(construct.getPowerModifier()).isZero();
        assertThat(construct.getToughnessModifier()).isZero();
    }

    private void addPermanentWithAbility(Player player, CardType type, CardEffect effect) {
        Card card = new Card();
        card.setName("Ability Source");
        card.setType(type);
        if (type == CardType.CREATURE) {
            card.setPower(1);
            card.setToughness(1);
        }
        card.addActivatedAbility(new ActivatedAbility(true, null, List.of(effect), "{T}: ability."));
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
    }
}
