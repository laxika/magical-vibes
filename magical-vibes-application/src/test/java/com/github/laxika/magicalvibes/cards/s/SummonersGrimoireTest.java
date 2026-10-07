package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshiokNightmareWeaver;
import com.github.laxika.magicalvibes.cards.b.BalefulEidolon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonersGrimoire.class, BalefulEidolon.class, GrizzlyBears.class, AshiokNightmareWeaver.class})
class SummonersGrimoireTest extends BaseCardTest {

    @Test
    @DisplayName("Job select creates and equips a Hero Shaman")
    void jobSelectCreatesAndEquipsHeroShaman() {
        harness.setHand(player1, List.of(new SummonersGrimoire()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent grimoire = findPermanent(player1, "Summoner's Grimoire");
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(grimoire.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero))
                .contains(CardSubtype.HERO, CardSubtype.SHAMAN);
    }

    @Test
    @DisplayName("An enchantment creature enters tapped and attacking")
    void enchantmentCreatureEntersTappedAndAttacking() {
        Permanent grimoire = addGrimoireReady(player1);
        Permanent attacker = addCreatureReady(player1);
        grimoire.setAttachedTo(attacker.getId());
        harness.setHand(player1, List.of(new BalefulEidolon()));

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        attackWith(attacker);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);

        harness.handleCardChosen(player1, 0);

        Permanent eidolon = findPermanent(player1, "Baleful Eidolon");
        assertThat(eidolon.isTapped()).isTrue();
        assertThat(eidolon.isAttackedThisTurn()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 3);
    }

    @Test
    @DisplayName("A nonenchantment creature enters normally")
    void nonenchantmentCreatureEntersNormally() {
        Permanent grimoire = addGrimoireReady(player1);
        Permanent attacker = addCreatureReady(player1);
        grimoire.setAttachedTo(attacker.getId());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        attackWith(attacker);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);

        harness.handleCardChosen(player1, 0);

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .filter(permanent -> !permanent.getId().equals(attacker.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(entered.isTapped()).isFalse();
        assertThat(entered.isAttacking()).isFalse();
        assertThat(entered.isAttackedThisTurn()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("The attack ability may be declined")
    void attackAbilityMayBeDeclined() {
        Permanent grimoire = addGrimoireReady(player1);
        Permanent attacker = addCreatureReady(player1);
        grimoire.setAttachedTo(attacker.getId());
        harness.setHand(player1, List.of(new BalefulEidolon()));

        attackWith(attacker);
        harness.handleCardChosen(player1, -1);

        harness.assertInHand(player1, "Baleful Eidolon");
        harness.assertNotOnBattlefield(player1, "Baleful Eidolon");
    }

    @Test
    @DisplayName("The attack ability cannot put a noncreature card onto the battlefield")
    void noncreatureHandDoesNotOfferChoice() {
        Permanent grimoire = addGrimoireReady(player1);
        Permanent attacker = addCreatureReady(player1);
        grimoire.setAttachedTo(attacker.getId());
        harness.setHand(player1, List.of(new SummonersGrimoire()));

        attackWith(attacker);

        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.assertInHand(player1, "Summoner's Grimoire");
        assertThat(findPermanents(player1, "Summoner's Grimoire")).hasSize(1);
    }

    @Test
    @DisplayName("Equipping moves the granted Shaman type to the new creature")
    void equippingMovesGrantedSubtype() {
        Permanent grimoire = addGrimoireReady(player1);
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        grimoire.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(grimoire.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.effectiveCreatureSubtypes(gd, first))
                .contains(CardSubtype.BEAR).doesNotContain(CardSubtype.SHAMAN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second))
                .contains(CardSubtype.BEAR, CardSubtype.SHAMAN);
    }

    @Test
    @DisplayName("An entering attacking creature offers a choice of defender")
    void enteringAttackerOffersDefenderChoice() {
        Permanent grimoire = addGrimoireReady(player1);
        Permanent attacker = addCreatureReady(player1);
        grimoire.setAttachedTo(attacker.getId());
        harness.enterBattlefieldAndReturn(player2, new AshiokNightmareWeaver());
        harness.setHand(player1, List.of(new BalefulEidolon()));

        attackWith(attacker);
        harness.handleCardChosen(player1, 0);

        PendingInteraction choice = gd.interaction.activeInteraction();
        assertThat(choice).isNotNull();
        assertThat(choice).isNotInstanceOf(PendingInteraction.BlockerDeclaration.class);
        assertThat(choice.decidingPlayerId()).isEqualTo(player1.getId());
    }

    private Permanent addGrimoireReady(Player player) {
        return addReadyPermanent(player, new SummonersGrimoire());
    }

    private Permanent addCreatureReady(Player player) {
        return addReadyPermanent(player, new GrizzlyBears());
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void attackWith(Permanent attacker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareAttackers(gd, player1, List.of(attackerIndex));
        harness.passBothPriorities();
    }
}
