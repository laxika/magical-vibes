package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Allay;
import com.github.laxika.magicalvibes.cards.f.Forbid;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Spellshock.class, ScareTactics.class, Forbid.class, RagingGoblin.class, Allay.class})
class SpellshockTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to an opponent who casts a spell")
    void damagesOpponentCastingSpell() {
        harness.addToBattlefield(player1, new Spellshock());
        harness.setHand(player2, List.of(new ScareTactics()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Deals 2 damage to its controller when they cast a spell")
    void damagesControllerCastingSpell() {
        harness.addToBattlefield(player1, new Spellshock());
        harness.setHand(player1, List.of(new ScareTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Triggers when a cast creature is countered")
    void triggersWhenCastSpellIsCountered() {
        harness.addToBattlefield(player1, new Spellshock());

        RagingGoblin goblin = new RagingGoblin();
        harness.setHand(player2, List.of(goblin));
        harness.addMana(player2, ManaColor.RED, 1);

        Forbid forbid = new Forbid();
        harness.setHand(player1, List.of(forbid));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int player1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int player2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, goblin.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1LifeBefore - 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2LifeBefore - 2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(goblin.getId()));
    }

    @Test
    @DisplayName("Damage resolves before the triggering creature spell")
    void damageResolvesBeforeCreatureSpell() {
        harness.addToBattlefield(player1, new Spellshock());
        RagingGoblin goblin = new RagingGoblin();
        harness.setHand(player2, List.of(goblin));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(goblin.getId()));

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(goblin.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Each Spellshock independently damages the spell's caster")
    void multipleSpellshocksEachTrigger() {
        harness.addToBattlefield(player1, new Spellshock());
        harness.addToBattlefield(player2, new Spellshock());
        harness.setHand(player1, List.of(new ScareTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        int casterLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(casterLifeBefore - 4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Spellshock does not trigger for its own casting")
    void doesNotTriggerForItsOwnCasting() {
        Spellshock spellshock = new Spellshock();
        harness.setHand(player1, List.of(spellshock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(spellshock.getId()));
    }

    @Test
    @DisplayName("A pending trigger still deals damage after Spellshock is destroyed")
    void pendingTriggerSurvivesSourceRemoval() {
        var spellshock = harness.addToBattlefieldAndReturn(player1, new Spellshock());
        harness.setHand(player2, List.of(new ScareTactics()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.setHand(player1, List.of(new Allay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int casterLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player2, 0);
        harness.castInstant(player1, 0, spellshock.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spellshock);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(casterLifeBefore - 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore - 2);

        harness.setHand(player2, List.of(new ScareTactics()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(casterLifeBefore - 2);
    }
}
