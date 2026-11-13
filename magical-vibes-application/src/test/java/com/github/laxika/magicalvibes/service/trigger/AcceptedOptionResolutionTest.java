package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentOfTriggeringPlayerMayDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExertAsItAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetArtifactCreatureThenMaySearchLibraryForNoncreatureArtifactEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An option a resolving spell or ability offers is carried out in that resolution (CR 608.2d), and "you may exert
 * this creature as it attacks" as the creature is declared as an attacker (CR 701.43a): neither puts an ability on
 * the stack once it is accepted. A triggered ability's own "you may" is the opposite case: the ability goes on the
 * stack first and asks as it resolves (CR 603.5).
 */
@CardUsed({GrizzlyBears.class})
class AcceptedOptionResolutionTest extends BaseCardTest {

    private static Card card(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost("{1}");
        return card;
    }

    private static Card creature(String name) {
        Card card = card(name, CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private void mainPhaseOf(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private List<StackEntry> abilitiesOnStack() {
        return gd.stack.stream().filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY).toList();
    }

    @Test
    @DisplayName("\"Each of that player's opponents may draw a card\": the draw is made as the ability resolves")
    void opponentsDrawAsTheAbilityResolves() {
        Card storyteller = creature("Fixture Storyteller");
        storyteller.addTrigger(Triggers.playerCastsSpell(), new EachOpponentOfTriggeringPlayerMayDrawCardEffect());
        harness.addToBattlefield(player1, storyteller);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        Card spell = card("Fixture Rite", CardType.SORCERY);
        spell.addSpellEffect(new GainLifeEffect(1));
        mainPhaseOf(player1);
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0);
        assertThat(abilitiesOnStack()).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(abilitiesOnStack()).isEmpty();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.parkedOuterResolutions).isEmpty();
    }

    @Test
    @DisplayName("An accepted option that asks for a card parks the spell that offered it, which then finishes resolving")
    void spellFinishesResolvingAfterAnAcceptedOptionThatAsked() {
        Card golem = creature("Fixture Golem");
        golem.setAdditionalTypes(java.util.Set.of(CardType.ARTIFACT));
        Permanent target = harness.addToBattlefieldAndReturn(player2, golem);
        Card trinket = card("Fixture Trinket", CardType.ARTIFACT);
        harness.setLibrary(player2, List.of(trinket));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Card spell = card("Fixture Salvage", CardType.SORCERY);
        spell.addSpellEffect(new SacrificeTargetArtifactCreatureThenMaySearchLibraryForNoncreatureArtifactEffect());
        spell.addSpellEffect(new DrawCardEffect(1));
        mainPhaseOf(player1);
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        assertThat(abilitiesOnStack()).isEmpty();
        assertThat(gd.parkedOuterResolutions).hasSize(1);
        assertThat(gd.parkedOuterResolutions.peek().entry().getCard()).isSameAs(spell);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Fixture Trinket");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.parkedOuterResolutions).isEmpty();
    }

    @Test
    @DisplayName("\"You may exert this creature as it attacks\" is asked as attackers are declared, not from the stack")
    void exertIsChosenAsTheCreatureAttacks() {
        Card champion = creature("Fixture Champion");
        champion.addStaticEffect(new ExertAsItAttacksEffect());
        champion.addTrigger(Triggers.exertedAsItAttacks(), new GainLifeEffect(3));
        Permanent attacker = addCreatureReady(player1, champion);

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.permanentsExertedThisTurn).contains(attacker.getId());
        assertThat(abilitiesOnStack()).hasSize(1);
        assertThat(abilitiesOnStack().getFirst().getEffectsToResolve()).containsExactly(new GainLifeEffect(3));
    }

    @Test
    @DisplayName("\"Whenever this attacks, you may gain 1 life\" goes on the stack and asks as it resolves")
    void triggeredMayIsAskedAsItResolves() {
        Card raider = creature("Fixture Raider");
        raider.addTrigger(Triggers.attacks(), new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
        addCreatureReady(player1, raider);
        int life = gd.getLife(player1.getId());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(abilitiesOnStack()).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(life + 1);
        assertThat(abilitiesOnStack()).isEmpty();
    }
}
