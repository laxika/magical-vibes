package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SummonersGrimoire.class, BalefulEidolon.class, GrizzlyBears.class})
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
        assertThat(eidolon.isAttackedThisTurn()).isTrue();
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

    private Permanent addGrimoireReady(Player player) {
        return addReadyPermanent(player, new SummonersGrimoire());
    }

    private Permanent addCreatureReady(Player player) {
        return addReadyPermanent(player, new GrizzlyBears());
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
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
