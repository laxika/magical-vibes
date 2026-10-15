package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
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

@CardUsed({MeanderedTowershell.class, GrizzlyBears.class, Naturalize.class})
class MeanderedTowershellTest extends BaseCardTest {

    @Test
    @DisplayName("Meandered Towershell grants islandwalk to the enchanted creature")
    void grantsIslandwalk() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castAuraOn(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.ISLANDWALK)).isTrue();
    }

    @Test
    @DisplayName("Attacking exiles the enchanted creature and Meandered Towershell")
    void attackingExilesBothCards() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = castAuraOn(bears);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(bears)
                .doesNotContain(aura);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(bears.getCard(), aura.getCard());
    }

    @Test
    @DisplayName("The creature returns tapped and attacking with Meandered Towershell attached on its next turn")
    void returnsCreatureAndAuraOnNextTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = castAuraOn(bears);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntil(TurnStep.DECLARE_ATTACKERS);
        harness.passBothPriorities();

        Permanent returnedBears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bears.getCard().getId()))
                .findFirst()
                .orElseThrow();
        Permanent returnedAura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(aura.getCard().getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returnedBears).isNotSameAs(bears);
        assertThat(returnedBears.isTapped()).isTrue();
        assertThat(returnedBears.isAttacking()).isTrue();
        assertThat(returnedBears.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(returnedAura.getAttachedTo()).isEqualTo(returnedBears.getId());
        assertThat(returnedAura.isTapped()).isFalse();
        assertThat(returnedAura.isAttacking()).isFalse();
        assertThat(gqs.hasKeyword(gd, returnedBears, Keyword.ISLANDWALK)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Destroying the Aura in response does not stop the creature's attack trigger")
    void creatureIsExiledEvenWhenAuraLeavesBeforeResolution() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = castAuraOn(bears);

        declareAttackers(List.of(0));
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura.getCard());
    }

    @Test
    @DisplayName("An attack trigger remembers the attacking creature when its Aura is moved")
    void movingAuraDoesNotChangeCreatureExiledByPendingTrigger() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = castAuraOn(attacker);

        declareAttackers(List.of(0));
        aura.setAttachedTo(otherCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(otherCreature)
                .doesNotContain(attacker, aura);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(attacker.getCard(), aura.getCard())
                .doesNotContain(otherCreature.getCard());
    }

    @Test
    @DisplayName("The delayed return waits through the opponent's declare attackers step")
    void doesNotReturnDuringOpponentsTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = castAuraOn(bears);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntil(TurnStep.DECLARE_ATTACKERS);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(bears.getCard(), aura.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears, aura);
    }

    private Permanent castAuraOn(Permanent creature) {
        harness.setHand(player1, List.of(new MeanderedTowershell()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        return findPermanent(player1, "Meandered Towershell");
    }
}
