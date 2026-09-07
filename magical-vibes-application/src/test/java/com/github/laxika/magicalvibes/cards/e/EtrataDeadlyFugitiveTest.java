package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EtrataDeadlyFugitive.class, GrizzlyBears.class, Divination.class, Island.class})
class EtrataDeadlyFugitiveTest extends BaseCardTest {

    @Test
    void assassinCombatDamageCloaksDamagedPlayersTopCardUnderItsControllersControl() {
        Card topCard = new GrizzlyBears();
        Permanent cloaked = resolveEtrataTrigger(topCard);

        assertThat(cloaked.isFaceDown()).isTrue();
        assertThat(cloaked.isCloaked()).isTrue();
        assertThat(cloaked.getCard()).isSameAs(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cloaked);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(cloaked);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(topCard);
    }

    @Test
    void nonAssassinCombatDamageDoesNotTriggerCloak() {
        harness.addToBattlefield(player1, new EtrataDeadlyFugitive());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isCloaked);
    }

    @Test
    void cloakedCreatureCanTurnFaceUpForTheGrantedAbilityCost() {
        Permanent cloaked = resolveEtrataTrigger(new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cloaked), 0, null, null);
        harness.passBothPriorities();

        assertThat(cloaked.isFaceDown()).isFalse();
        assertThat(cloaked.isCloaked()).isFalse();
    }

    @Test
    void instantOrSorceryIsExiledAndMayBeCastForFreeWhenItCannotTurnFaceUp() {
        Card topCard = new Divination();
        Permanent cloaked = resolveEtrataTrigger(topCard);
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cloaked), 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cloaked);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        harness.assertInGraveyard(player2, "Divination");
    }

    private Permanent resolveEtrataTrigger(Card topCard) {
        harness.setLibrary(player2, List.of(topCard, new Island()));
        Permanent etrata = addCreatureReady(player1, new EtrataDeadlyFugitive());
        etrata.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isCloaked)
                .findFirst()
                .orElseThrow();
    }
}
