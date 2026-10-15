package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CorsairCaptain;
import com.github.laxika.magicalvibes.cards.k.KinjallisSunwing;
import com.github.laxika.magicalvibes.cards.l.LlanowarReborn;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RamirezDePietroPillager.class, CorsairCaptain.class, KinjallisSunwing.class, LlanowarReborn.class})
class RamirezDePietroPillagerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two Treasure tokens and makes its controller lose 2 life")
    void entersWithLifeLossAndTwoTreasures() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new RamirezDePietroPillager()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("A Pirate dealing combat damage exiles the top card and its cast permission persists after Ramirez leaves")
    void pirateCombatDamageExilesAndAllowsCasting() {
        Permanent ramirez = addAttackingCreature(player1, new RamirezDePietroPillager());
        Card topCard = new KinjallisSunwing();
        harness.setLibrary(player2, List.of(topCard, new LlanowarReborn()));

        resolveCombatAndTrigger();

        assertThat(gd.getCardsExiledByPermanent(ramirez.getId())).containsExactly(topCard);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ramirez));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        harness.assertOnBattlefield(player1, "Kinjalli's Sunwing");
    }

    @Test
    @DisplayName("The trigger batches multiple Pirates dealing combat damage in one damage step")
    void multiplePiratesCauseOnlyOneExile() {
        Permanent ramirez = addAttackingCreature(player1, new RamirezDePietroPillager());
        addAttackingCreature(player1, new CorsairCaptain());
        Card first = new KinjallisSunwing();
        Card second = new LlanowarReborn();
        harness.setLibrary(player2, List.of(first, second));

        resolveCombatAndTrigger();

        assertThat(gd.getCardsExiledByPermanent(ramirez.getId())).containsExactly(first);
    }

    @Test
    @DisplayName("Combat damage from a non-Pirate does not trigger the ability")
    void nonPirateCombatDamageDoesNotTrigger() {
        Permanent ramirez = addCreatureReady(player1, new RamirezDePietroPillager());
        addAttackingCreature(player1, new KinjallisSunwing());
        Card topCard = new LlanowarReborn();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();

        assertThat(gd.getCardsExiledByPermanent(ramirez.getId())).isEmpty();
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("An exiled land cannot be played while Ramirez is on the battlefield")
    void cannotPlayExiledLand() {
        addAttackingCreature(player1, new RamirezDePietroPillager());
        Card land = new LlanowarReborn();
        harness.setLibrary(player2, List.of(land));

        resolveCombatAndTrigger();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Llanowar Reborn");
    }

    @Test
    @DisplayName("An exiled land cannot be played after Ramirez leaves the battlefield")
    void cannotPlayExiledLandAfterSourceLeaves() {
        Permanent ramirez = addAttackingCreature(player1, new RamirezDePietroPillager());
        Card land = new LlanowarReborn();
        harness.setLibrary(player2, List.of(land));

        resolveCombatAndTrigger();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ramirez));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Llanowar Reborn");
    }

    @Test
    @DisplayName("Gaining control of Ramirez does not grant access to cards exiled by its previous controller")
    void newControllerCannotCastPreviouslyExiledCard() {
        Permanent ramirez = addAttackingCreature(player1, new RamirezDePietroPillager());
        Card topCard = new KinjallisSunwing();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();
        gd.playerBattlefields.get(player1.getId()).remove(ramirez);
        gd.playerBattlefields.get(player2.getId()).add(ramirez);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player2, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();

        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kinjalli's Sunwing");
    }

    @Test
    @DisplayName("The trigger still exiles and grants casting permission when Ramirez leaves before it resolves")
    void sourceLeavesBeforeTriggerResolves() {
        Permanent ramirez = addAttackingCreature(player1, new RamirezDePietroPillager());
        Card topCard = new KinjallisSunwing();
        harness.setLibrary(player2, List.of(topCard));

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ramirez));
        resolveAllTriggers();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kinjalli's Sunwing");
    }

    @Test
    @DisplayName("An empty damaged player's library does not prevent the trigger from resolving")
    void emptyLibraryExilesNothing() {
        Permanent ramirez = addAttackingCreature(player1, new RamirezDePietroPillager());
        harness.setLibrary(player2, List.of());

        resolveCombatAndTrigger();

        assertThat(gd.getCardsExiledByPermanent(ramirez.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting an exiled creature still requires its normal colored mana cost")
    void cannotSpendManaOfAnyColor() {
        addAttackingCreature(player1, new RamirezDePietroPillager());
        Card topCard = new KinjallisSunwing();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    @DisplayName("Casting an exiled creature still requires normal sorcery timing")
    void cannotCastExiledCreatureDuringCombat() {
        addAttackingCreature(player1, new RamirezDePietroPillager());
        Card topCard = new KinjallisSunwing();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    @DisplayName("An opponent's Pirate dealing combat damage does not trigger Ramirez")
    void opponentPirateDoesNotTrigger() {
        Permanent ramirez = addCreatureReady(player1, new RamirezDePietroPillager());
        addAttackingCreature(player2, new CorsairCaptain());
        Card topCard = new LlanowarReborn();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.getCardsExiledByPermanent(ramirez.getId())).isEmpty();
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    private Permanent addAttackingCreature(Player player, Card card) {
        Permanent creature = addCreatureReady(player, card);
        creature.setAttacking(true);
        return creature;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
