package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Slimefoot.class, Shock.class, GrizzlyBears.class, MaskwoodNexus.class})
class SlimefootTest extends BaseCardTest {

    @Test
    @DisplayName("Token creation requires four mana")
    void tokenCreationRequiresFourMana() {
        harness.addToBattlefield(player1, new Slimefoot());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Saproling")).isZero();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Saproling")).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activating ability puts token creation on the stack")
    void activatingAbilityPutsOnStack() {
        addSlimefootReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isSameAs(findPermanent(player1, "Slimefoot, the Stowaway").getCard());
    }

    @Test
    @DisplayName("Resolving ability creates a 1/1 green Saproling token")
    void resolvingAbilityCreatesToken() {
        addSlimefootReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent token = findPermanent(player1, "Saproling");
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
    }

    @Test
    @DisplayName("Deals 1 damage to each opponent and gains 1 life when a Saproling dies")
    void triggersWhenSaprolingDies() {
        addSlimefootReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        // Create a Saproling token
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        int p1LifeBefore = gd.getLife(player1.getId());
        int p2LifeBefore = gd.getLife(player2.getId());

        // Kill the Saproling with Shock
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        java.util.UUID saprolingId = harness.getPermanentId(player1, "Saproling");
        harness.castAndResolveInstant(player2, 0, saprolingId); // Resolve Shock → Saproling dies → death trigger
        harness.passBothPriorities(); // Resolve Slimefoot's trigger

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(p1LifeBefore + 1);
    }

    @Test
    @DisplayName("Does NOT trigger when a non-Saproling creature dies")
    void doesNotTriggerForNonSaproling() {
        addSlimefootReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        int p1LifeBefore = gd.getLife(player1.getId());
        int p2LifeBefore = gd.getLife(player2.getId());

        // Kill the Bears with Shock
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        java.util.UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, bearsId); // Resolve Shock → Bears die

        // No trigger should have fired — life totals unchanged
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore);
        assertThat(gd.getLife(player1.getId())).isEqualTo(p1LifeBefore);
    }

    @Test
    @DisplayName("Triggers multiple times when multiple Saprolings die")
    void triggersForEachSaprolingDeath() {
        addSlimefootReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        // Create two Saproling tokens
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        long saprolingCount = countPermanents(player1, "Saproling");
        assertThat(saprolingCount).isEqualTo(2);

        int p1LifeBefore = gd.getLife(player1.getId());
        int p2LifeBefore = gd.getLife(player2.getId());

        // Kill first Saproling
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        java.util.UUID saproling1Id = harness.getPermanentId(player1, "Saproling");
        harness.castAndResolveInstant(player2, 0, saproling1Id); // Resolve Shock → Saproling dies → death trigger
        harness.passBothPriorities(); // Resolve Slimefoot's trigger

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(p1LifeBefore + 1);

        // Kill second Saproling
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        java.util.UUID saproling2Id = harness.getPermanentId(player1, "Saproling");
        harness.castAndResolveInstant(player2, 0, saproling2Id); // Resolve Shock → Saproling dies → death trigger
        harness.passBothPriorities(); // Resolve Slimefoot's trigger

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(p1LifeBefore + 2);
    }

    @Test
    @DisplayName("Can create a token while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent slimefoot = harness.addToBattlefieldAndReturn(player1, new Slimefoot());
        slimefoot.setSummoningSick(true);
        slimefoot.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Saproling")).isEqualTo(1);
        assertThat(slimefoot.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opposing Saproling dying does not trigger Slimefoot")
    void doesNotTriggerForOpposingSaproling() {
        addSlimefootReady(player1);
        addSlimefootReady(player2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player2, "Saproling"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Still triggers for Saprolings dying simultaneously with Slimefoot")
    void triggersWhenSourceAndSaprolingDieSimultaneously() {
        Permanent slimefoot = addSlimefootReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent saproling = findPermanent(player1, "Saproling");
        slimefoot.setMarkedDamage(3);
        saproling.setMarkedDamage(1);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Slimefoot, the Stowaway");
        harness.assertNotOnBattlefield(player1, "Saproling");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Triggers for its own death when Slimefoot is a Saproling")
    void triggersForOwnDeathWhenSlimefootIsSaproling() {
        addSlimefootReady(player1);
        harness.addToBattlefield(player1, new MaskwoodNexus());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        java.util.UUID slimefootId = harness.getPermanentId(player1, "Slimefoot, the Stowaway");
        harness.castAndResolveInstant(player2, 0, slimefootId);
        assertThat(gd.stack).isEmpty();

        harness.castAndResolveInstant(player2, 0, slimefootId);

        harness.assertNotOnBattlefield(player1, "Slimefoot, the Stowaway");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    private Permanent addSlimefootReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new Slimefoot());
        perm.setSummoningSick(false);
        return perm;
    }
}
