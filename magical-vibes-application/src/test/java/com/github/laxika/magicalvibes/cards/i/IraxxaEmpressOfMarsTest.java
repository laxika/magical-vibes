package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IraxxaEmpressOfMars.class, GrizzlyBears.class, ThinkTwice.class})
class IraxxaEmpressOfMarsTest extends BaseCardTest {

    @Test
    @DisplayName("Paradox creates a 2/2 red Alien Warrior when casting from exile")
    void paradoxCreatesAlienWarriorWhenCastingFromExile() {
        harness.addToBattlefield(player1, new IraxxaEmpressOfMars());
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromExile(player1, spell.getId());
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Alien Warrior");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ALIEN, CardSubtype.WARRIOR);
    }

    @Test
    @DisplayName("Casting a spell from hand does not trigger Paradox")
    void handSpellDoesNotTriggerParadox() {
        harness.addToBattlefield(player1, new IraxxaEmpressOfMars());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Alien Warrior")).isEmpty();
    }

    @Test
    @DisplayName("Battle cry gives +1/+0 to other attacking creatures")
    void battleCryBoostsOtherAttackers() {
        addCreatureReady(player1, new IraxxaEmpressOfMars());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void paradoxResolvesBeforeTheSpellAndSurvivesItsSourceLeaving() {
        harness.addToBattlefield(player1, new IraxxaEmpressOfMars());
        GrizzlyBears spell = new GrizzlyBears();
        harness.setExile(player1, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromExile(player1, spell.getId());
        assertThat(gd.stack).hasSize(2);
        assertThat(findPermanents(player1, "Alien Warrior")).isEmpty();
        gd.playerGraveyards.get(player1.getId()).add(
                gd.playerBattlefields.get(player1.getId()).removeFirst().getCard());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Alien Warrior")).hasSize(1);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    void flashbackTriggersParadox() {
        harness.addToBattlefield(player1, new IraxxaEmpressOfMars());
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Alien Warrior")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName()).contains("Grizzly Bears");
    }

    @Test
    void opponentCastingFromExileDoesNotTriggerParadox() {
        harness.addToBattlefield(player1, new IraxxaEmpressOfMars());
        GrizzlyBears spell = new GrizzlyBears();
        harness.setExile(player2, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player2.getId());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castFromExile(player2, spell.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Alien Warrior")).isEmpty();
        assertThat(findPermanents(player2, "Alien Warrior")).isEmpty();
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
    }

    @Test
    void battleCryExcludesSourceAndNonattackersAndExpiresAtCleanup() {
        Permanent iraxxa = addCreatureReady(player1, new IraxxaEmpressOfMars());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(iraxxa.getEffectivePower()).isEqualTo(5);
        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(nonattacker.getEffectivePower()).isEqualTo(2);
        assertThat(opponent.getEffectivePower()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
    }

    @Test
    void trampleDealsExcessDamageThroughABlocker() {
        harness.setLife(player2, 20);
        Permanent iraxxa = addCreatureReady(player1, new IraxxaEmpressOfMars());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 3));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(iraxxa);
        assertThat(iraxxa.getMarkedDamage()).isEqualTo(2);
    }
}
